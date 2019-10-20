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

var (
	data helpers.ViewData
	sw   helpers.Switch
)

//SavePos saves position of switch in db
func SavePos(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")
	top := r.FormValue("top")
	left := r.FormValue("left")

	_, err := server.Core.DBdst.Exec("UPDATE switches SET (postop, posleft) = ($1, $2) WHERE name = $3", top, left, name)
	if err != nil {
		log.Printf("Error updating position of switch %s: %s", name, err)
	}

	_, err = w.Write([]byte("success"))
	if err != nil {
		log.Printf("Error writing answer for switch position update: %s", err)
	}
}

//GetMap make and send map of switches
func GetMap(w http.ResponseWriter, r *http.Request) {
	vis := make(map[string][]string)

	rows, err := server.Core.DBdst.Query("SELECT name from switches")
	if err != nil {
		log.Println("Error with making query to show visualization")
		http.NotFound(w, r)
		return
	}
	defer rows.Close()

	for rows.Next() {
		var s string

		err := rows.Scan(&s)
		if err != nil {
			log.Println("Error with scanning database to show visualization: ", err)
		}

		subrows, err := server.Core.DBdst.Query("SELECT name FROM switches WHERE upswitch = $1", s)
		if err != nil {
			log.Println("Error with making query to find upswitches to show visualization")
			http.NotFound(w, r)
			return
		}

		var downSwitches []string

		for subrows.Next() {
			var downSwitch string

			err := subrows.Scan(&downSwitch)
			if err != nil {
				log.Println("Error with scanning database to find upswitches to show visualization: ", err)
			}

			downSwitches = append(downSwitches, downSwitch)
		}
		if err = subrows.Err(); err != nil {
			log.Println("Error searching switch names for visualization: ", err)
		}

		err = subrows.Close()
		if err != nil {
			log.Printf("Error closing subrows for %s switch: %s", s, err)
		}

		vis[s] = downSwitches
	}
	if err = rows.Err(); err != nil {
		log.Println("Error searching switch names for visualization: ", err)
	}

	if r.Method == "GET" {
		m, err := json.MarshalIndent(vis, "", "  ")
		if err != nil {
			log.Println("Error marshalling data to send: ", err)
		}

		w.Header().Set("Content-Type", "application/json")

		_, err = w.Write(m)
		if err != nil {
			log.Printf("Error writing map of switches for visualization: %s", err)
		}
	}
}

//VisHandler handle page with visualization
func VisHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	data = helpers.ViewData{
		User: session.Values["user"],
	}

	tmpl, err := template.ParseFiles("templates/vis.html")
	if err != nil {
		log.Printf("Error parsing template files for visualization page: %s", err)
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for visualization page: %s", err)
	}
}

//MapHandler handle main page map
func MapHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	if r.Method == "GET" {
		buildings := []helpers.Build{}

		data = helpers.ViewData{
			User:   session.Values["user"],
			Builds: buildings,
		}

		dbbuilds, err := server.Core.DBdst.Query("SELECT name, addr FROM buildings")
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

		tmpl, err := template.ParseFiles("templates/map.html")
		if err != nil {
			log.Printf("Error parsing template files for map page: %s", err)
		}

		err = tmpl.Execute(w, data)
		if err != nil {
			log.Printf("Error executing template for map page: %s", err)
		}
	}
}

//BuildHandler handle map/build pages
func BuildHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	floors := []helpers.Floor{}
	vars := mux.Vars(r)
	build := vars["build"]

	data = helpers.ViewData{
		Build:  build,
		Floors: floors,
		User:   session.Values["user"],
	}

	if r.Method == "GET" {
		dbfloors, err := server.Core.DBdst.Query("SELECT build, floor FROM floors WHERE build = $1", build)
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

		tmpl, err := template.ParseFiles("templates/build.html")
		if err != nil {
			log.Printf("Error parsing template files for %s build page: %s", build, err)
		}

		err = tmpl.Execute(w, data)
		if err != nil {
			log.Printf("Error executing template for %s build page: %s", build, err)
		}
	}
}

//FloorHandler handle map/build/floor pages
func FloorHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

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

			dbswits, err := server.Core.DBdst.Query("SELECT name, ip, mac, serial, model, upswitch, build, floor, postop, posleft FROM switches WHERE build = $1 AND floor = $2", build, floor)
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

			tmpl, err := template.ParseFiles("templates/plan.html")
			if err != nil {
				log.Printf("Error parsing template files for plan page: %s", err)
			}

			err = tmpl.Execute(w, data)
			if err != nil {
				log.Printf("Error executing template for plan page: %s", err)
			}
		}
	} else if os.IsNotExist(err) {
		http.Redirect(w, r, "/planupdate/"+build+"/"+floor, http.StatusFound)
	}
}

//ListHandler handle page with list of hosts
func ListHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	switches := []helpers.Switch{}

	data = helpers.ViewData{
		User:  session.Values["user"],
		Swits: switches,
	}

	dblist, err := server.Core.DBdst.Query("SELECT name, ip, mac, serial, model, upswitch, build, floor FROM switches")
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

		dbbuild, err := server.Core.DBdst.Query("SELECT name FROM buildings WHERE addr = $1", swit.Build)
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

	tmpl, err := template.ParseFiles("templates/list.html")
	if err != nil {
		log.Printf("Error parsing template files for list of switches page: %s", err)
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for list of switches page: %s", err)
	}
}

//ChangePage shows change page
func ChangePage(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	vars := mux.Vars(r)
	sw := vars["switch"]

	data = helpers.ViewData{
		User: session.Values["user"],
	}

	dbswits, err := server.Core.DBdst.Query("SELECT ip, mac, revision, serial, model, upswitch FROM switches WHERE name = $1", sw)
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

	tmpl, err := template.ParseFiles("templates/change.html")
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
	upswitch := r.FormValue("upswitch")

	_, err = server.Core.DBdst.Exec("UPDATE switches SET (ip, mac, upswitch) = ($1, $2, $3) WHERE name = $4", ip, mac, upswitch, sw)
	if err != nil {
		log.Printf("Error changing %s switch data: %s", sw, err)
	}

	http.Redirect(w, r, "/list", http.StatusFound)
}

//LogsHandler handle logs page
func LogsHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	data = helpers.ViewData{
		User: session.Values["user"],
	}

	tmpl, err := template.ParseFiles("templates/logs.html")
	if err != nil {
		log.Printf("Error parsing template files for logs page: %s", err)
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		log.Printf("Error executing template for logs page: %s", err)
	}
}
