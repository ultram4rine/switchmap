package handler

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

var (
	data helpers.ViewData
	sw   helpers.Switch
)

//SavePos saves position of switch in db
func SavePos(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	name := r.FormValue("name")
	top := r.FormValue("top")
	left := r.FormValue("left")

	_, err := server.Core.DB1.Exec("UPDATE `host` set postop = ?, posleft = ? WHERE name = ?", top, left, name)
	if err != nil {
		log.Printf("Error updating position of switch %s: %s", name, err)
	}
	w.Write([]byte("success"))
}

//GetMap make and send map of switches
func GetMap(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	vis := make(map[string][]string)

	dbvis, err := server.Core.DB1.Query("SELECT name from host")
	if err != nil {
		log.Println("Error with making query to show visualization")
	}
	defer dbvis.Close()

	for dbvis.Next() {
		var s string

		err := dbvis.Scan(&s)
		if err != nil {
			log.Println("Error with scanning database to show visualization: ", err)
		}

		dbvisup, err := server.Core.DB1.Query("SELECT name from host WHERE upswitch = ?", s)
		if err != nil {
			log.Println("Error with making query to find upswitches to show visualization")
		}
		defer dbvisup.Close()

		var ups []string

		for dbvisup.Next() {
			var up string

			err := dbvisup.Scan(&up)
			if err != nil {
				log.Println("Error with scanning database to find upswitches to show visualization: ", err)
			}

			ups = append(ups, up)
		}

		vis[s] = ups
	}

	if r.Method == "GET" {
		m, err := json.MarshalIndent(vis, "", "  ")
		if err != nil {
			log.Println("Error marshalling data to send: ", err)
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write(m)
	}
}

//VisHandler handle page with visualization
func VisHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	data = helpers.ViewData{
		User: session.Values["user"],
	}

	tmpl, _ := template.ParseFiles("templates/vis.html")
	tmpl.Execute(w, data)
}

//MapHandler handle main page map
func MapHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	if r.Method == "GET" {
		buildings := []helpers.Build{}

		data = helpers.ViewData{
			User:   session.Values["user"],
			Builds: buildings,
		}

		dbbuilds, err := server.Core.DB1.Query("SELECT `name`, `addr` from `buildings` WHERE `hidden` = ?", 0)
		if err != nil {
			log.Println("Error with making query to show builds: ", err)
		}
		defer dbbuilds.Close()

		for dbbuilds.Next() {
			b := helpers.Build{}

			err := dbbuilds.Scan(&b.Name, &b.Address)
			if err != nil {
				log.Println("Error with scanning database to show builds: ", err)
			}

			data.Builds = append(data.Builds, b)
		}

		sort.Slice(data.Builds, func(i, j int) bool {
			k, _ := strconv.Atoi(data.Builds[i].Address[1:])
			l, _ := strconv.Atoi(data.Builds[j].Address[1:])
			return k < l
		})

		tmpl, _ := template.ParseFiles("templates/map.html")
		tmpl.Execute(w, data)
	}
}

//BuildHandler handle map/build pages
func BuildHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	floors := []helpers.Floor{}
	vars := mux.Vars(r)
	build := vars["build"]
	data = helpers.ViewData{
		Build:  build,
		Floors: floors,
		User:   session.Values["user"],
	}

	if r.Method == "GET" {
		dbfloors, err := server.Core.DB1.Query("SELECT `build`, `floor` from `floors` WHERE `build` = ? AND `hidden` = ?", build, 0)
		if err != nil {
			log.Println("Error with making query to show floors: ", err)
		}
		defer dbfloors.Close()

		for dbfloors.Next() {
			f := helpers.Floor{}

			err := dbfloors.Scan(&f.Build, &f.Floor)
			if err != nil {
				log.Println("Error with scanning database to show floors: ", err)
			}

			data.Floors = append(data.Floors, f)
		}

		sort.Slice(data.Floors, func(i, j int) bool {
			return data.Floors[i].Floor < data.Floors[j].Floor
		})

		tmpl, _ := template.ParseFiles("templates/build.html")
		tmpl.Execute(w, data)
	}
}

//FloorHandler handle map/build/floor pages
func FloorHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	vars := mux.Vars(r)
	build := vars["build"]
	floor := vars["floor"]
	planpath := fmt.Sprintf("private/plans/%s%s.png", build, floor)

	if _, err := os.Stat(planpath); err == nil {
		if r.Method == "GET" {
			var switches []helpers.Switch

			data = helpers.ViewData{
				Build: build,
				Floor: floor,
				User:  session.Values["user"],
				Swits: switches,
			}

			dbswits, err := server.Core.DB1.Query("SELECT `name`, `ip`, `mac`, `serial`, `model`, `upswitch`, `build`, `floor`, `postop`, `posleft` from `host` WHERE build = ? AND floor = ?", build, floor)
			if err != nil {
				log.Println("Error with making query to show list of switches: ", err)
			}
			defer dbswits.Close()

			for dbswits.Next() {
				swit := helpers.Switch{}

				err := dbswits.Scan(&swit.Name, &swit.IP, &swit.MAC, &swit.Serial, &swit.Model, &swit.Upswitch, &swit.Build, &swit.Floor, &swit.Postop, &swit.Posleft)
				if err != nil {
					log.Println("Error with scanning database to show list of switches: ", err)
				}

				data.Swits = append(data.Swits, swit)
			}

			tmpl, _ := template.ParseFiles("templates/plan.html")
			tmpl.Execute(w, data)
		}
	} else if os.IsNotExist(err) {
		http.Redirect(w, r, "/planupdate/"+build+"/"+floor, 301)
	}
}

//ListHandler handle page with list of hosts
func ListHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	switches := []helpers.Switch{}

	data = helpers.ViewData{
		User:  session.Values["user"],
		Swits: switches,
	}

	dblist, err := server.Core.DB1.Query("SELECT `name`, `ip`, `mac`, `serial`, `model`, `upswitch`, `build`, `floor` from `host`")
	if err != nil {
		log.Println("Error with making query to show list of switches: ", err)
	}
	defer dblist.Close()

	for dblist.Next() {
		swit := helpers.Switch{}

		err := dblist.Scan(&swit.Name, &swit.IP, &swit.MAC, &swit.Serial, &swit.Model, &swit.Upswitch, &swit.Build, &swit.Floor)
		if err != nil {
			log.Println("Error with scanning database to show list of switches: ", err)
		}

		dbbuild, err := server.Core.DB1.Query("SELECT `name` from `buildings` WHERE addr = ?", swit.Build)
		if err != nil {
			log.Println("Error with making query to find build name: ", err)
		}
		defer dbbuild.Close()

		for dbbuild.Next() {
			err := dbbuild.Scan(&swit.Build)
			if err != nil {
				log.Println("Error with scanning database to show list of switches: ", err)
			}
		}

		swit.Floor = swit.Floor[1:] + " этаж"
		data.Swits = append(data.Swits, swit)
	}

	tmpl, _ := template.ParseFiles("templates/list.html")
	tmpl.Execute(w, data)
}

//ChangePage shows change page
func ChangePage(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 301)
		return
	}
	session, _ := server.Core.Store.Get(r, "switchmap_session")

	vars := mux.Vars(r)
	sw := vars["switch"]

	data = helpers.ViewData{
		User: session.Values["user"],
	}

	dbswits, err := server.Core.DB1.Query("SELECT `ip`, `mac`, `revision`, `serial`, `model`, `upswitch` from `host` WHERE name = ?", sw)
	if err != nil {
		log.Println("Error with making query to show list of switches: ", err)
	}
	defer dbswits.Close()

	for dbswits.Next() {
		err := dbswits.Scan(&data.Sw.IP, &data.Sw.MAC, &data.Sw.Revision, &data.Sw.Serial, &data.Sw.Model, &data.Sw.Upswitch)
		if err != nil {
			log.Println("Error with scanning switchmap database to get switch info: ", err)
		}
		data.Sw.Name = sw
	}

	tmpl, _ := template.ParseFiles("templates/change.html")
	tmpl.Execute(w, data)
}

//ChangeHandler handle change page
func ChangeHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 301)
		return
	}

	vars := mux.Vars(r)
	sw := vars["switch"]

	err := r.ParseForm()
	if err != nil {
		log.Println("Error parsing form from change page: ", err)
	}

	ip := r.FormValue("IP")
	mac := r.FormValue("MAC")
	upswitch := r.FormValue("upswitch")

	_, err = server.Core.DB1.Exec("UPDATE host set ip = ?, mac = ?, upswitch = ? WHERE name = ?", ip, mac, upswitch, sw)

	http.Redirect(w, r, "/list", 301)
}

//LogsHandler handle logs page
func LogsHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 301)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	data = helpers.ViewData{
		User: session.Values["user"],
	}
	tmpl, _ := template.ParseFiles("templates/logs.html")
	tmpl.Execute(w, data)
}
