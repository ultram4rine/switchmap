package handlers

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"sort"
	"strconv"
	"text/template"

	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/gorilla/mux"
)

//RootHandler handle / path
func RootHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", http.StatusFound)
	} else {
		http.Redirect(w, r, "/map", http.StatusFound)
	}
}

//PrivateHandler handle private files
func PrivateHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", http.StatusFound)
		return
	}

	realHandler := http.StripPrefix("/private/", http.FileServer(http.Dir("./private/"))).ServeHTTP
	realHandler(w, r)
}

//SavePos saves position of switch in db
func SavePos(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")
	top := r.FormValue("top")
	left := r.FormValue("left")

	_, err := server.Core.DBdst.Exec("UPDATE switches SET (postop, posleft) = ($1, $2) WHERE name = $3", top, left, name)
	if err != nil {
		log.Printf("Error updating position of switch %s: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

//GetMap make and send map of switches
func GetMap(w http.ResponseWriter, r *http.Request) {
	visMap, err := helpers.MakeVisMap()
	if err != nil {
		log.Printf("Error making map for visualization: %s", err)
		http.Redirect(w, r, "/map", http.StatusFound)
		return
	}

	visMapJSON, err := json.Marshal(visMap)
	if err != nil {
		log.Printf("Error marshalling map for sending: %s", err)
		http.Redirect(w, r, "/map", http.StatusFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")

	_, err = w.Write(visMapJSON)
	if err != nil {
		log.Printf("Error writing map of switches for visualization: %s", err)
		http.Redirect(w, r, "/map", http.StatusFound)
		return
	}
}

//VisHandler handle page with visualization
func VisHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for visualization page: %s", err)
		return
	}

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/vis.html")
	if err != nil {
		log.Printf("Error parsing template files for visualization page: %s", err)
		http.Redirect(w, r, "/map", http.StatusFound)
		return
	}

	data := helpers.ViewData{
		User: session.Values["user"],
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for visualization page: %s", err)
		http.Redirect(w, r, "/map", http.StatusFound)
		return
	}
}

//MapHandler handle main page map
func MapHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for map page: %s", err)
		return
	}

	var buildings []helpers.Build

	err = server.Core.DBdst.Select(&buildings, "SELECT name, addr FROM buildings")
	if err != nil {
		log.Printf("Error getting builds to show it on map: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	sort.Slice(buildings, func(i, j int) bool {
		k, _ := strconv.Atoi(buildings[i].Address[1:])
		l, _ := strconv.Atoi(buildings[j].Address[1:])
		return k < l
	})

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/map.html")
	if err != nil {
		log.Printf("Error parsing template files for map page: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	data := helpers.ViewData{
		User:   session.Values["user"],
		Builds: buildings,
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for map page: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

//BuildHandler handle map/build pages
func BuildHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for build page: %s", err)
		return
	}

	vars := mux.Vars(r)
	build := vars["build"]

	var floors []helpers.Floor

	err = server.Core.DBdst.Select(&floors, "SELECT build, floor FROM floors WHERE build = $1", build)
	if err != nil {
		log.Printf("Error getting floors to show it on build: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	sort.Slice(floors, func(i, j int) bool {
		return floors[i].Floor < floors[j].Floor
	})

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/build.html")
	if err != nil {
		log.Printf("Error parsing template files for %s build page: %s", build, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	data := helpers.ViewData{
		User:   session.Values["user"],
		Build:  build,
		Floors: floors,
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for %s build page: %s", build, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

//FloorHandler handle map/build/floor pages
func FloorHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for floor page: %s", err)
		return
	}

	vars := mux.Vars(r)
	build := vars["build"]
	floor := vars["floor"]

	planPath := fmt.Sprintf("private/plans/%s%s.png", build, floor)

	if _, err := os.Stat(planPath); err == nil {
		var switches []helpers.Switch

		err := server.Core.DBdst.Select(&switches, "SELECT name, ip, mac, serial, model, upswitch, build, floor, postop, posleft FROM switches WHERE build = $1 AND floor = $2", build, floor)
		if err != nil {
			log.Printf("Error getting switches for show it on floor: %s", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		tmpl, err := template.ParseFiles("templates/layout.html", "templates/plan.html")
		if err != nil {
			log.Printf("Error parsing template files for plan page: %s", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		data := helpers.ViewData{
			User:  session.Values["user"],
			Build: build,
			Floor: floor,
			Swits: switches,
		}

		err = tmpl.Execute(w, data)
		if err != nil {
			log.Printf("Error executing template for plan page: %s", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	} else if os.IsNotExist(err) {
		http.Redirect(w, r, "/planupdate/"+build+"/"+floor, http.StatusFound)
	}
}

//ListHandler handle page with list of hosts
func ListHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for list of switches page: %s", err)
		return
	}

	var switches []helpers.Switch

	err = server.Core.DBdst.Select(&switches, "SELECT name, ip, mac, serial, model, upswitch, build, floor FROM switches")
	if err != nil {
		log.Printf("Error getting switches to show list: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	for _, sw := range switches {
		var build helpers.Build

		err = server.Core.DBdst.Get(&build, "SELECT name FROM buildings WHERE addr = $1", sw.Build)
		if err != nil {
			log.Printf("Error getting build name for %s switch: %s", sw.Name, err)
		}

		sw.Build = build.Name
	}

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/list.html")
	if err != nil {
		log.Printf("Error parsing template files for list of switches page: %s", err)
	}

	data := helpers.ViewData{
		User:  session.Values["user"],
		Swits: switches,
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for list of switches page: %s", err)
	}
}

//ChangePage shows change page
func ChangePage(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for change switch page: %s", err)
		return
	}

	vars := mux.Vars(r)
	swName := vars["switch"]

	var sw helpers.Switch

	err = server.Core.DBdst.Get(&sw, "SELECT ip, mac, revision, serial, model, upswitch FROM switches WHERE name = $1", swName)
	if err != nil {
		log.Printf("Error getting info about %s switch: %s", swName, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	sw.Name = swName
	data := helpers.ViewData{
		User: session.Values["user"],
		Sw:   sw,
	}

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/change.html")
	if err != nil {
		log.Printf("Error parsing template files for change switch page: %s", err)
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for change switch page: %s", err)
	}
}

//ChangeHandler handle change page
func ChangeHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	sw := vars["switch"]

	err := r.ParseForm()
	if err != nil {
		log.Println("Error parsing form from change page: ", err)
	}

	ip := r.FormValue("IP")
	mac := r.FormValue("MAC")
	upSwitch := r.FormValue("upswitch")

	_, err = server.Core.DBdst.Exec("UPDATE switches SET (ip, mac, upswitch) = ($1, $2, $3) WHERE name = $4", ip, mac, upSwitch, sw)
	if err != nil {
		log.Printf("Error changing %s switch data: %s", sw, err)
	}

	http.Redirect(w, r, "/list", http.StatusFound)
}

//LogsHandler handle logs page
func LogsHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for logs page: %s", err)
		return
	}

	tmpl, err := template.ParseFiles("templates/layout.html", "templates/logs.html")
	if err != nil {
		log.Printf("Error parsing template files for logs page: %s", err)
	}

	data := helpers.ViewData{
		User: session.Values["user"],
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for logs page: %s", err)
	}
}
