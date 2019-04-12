package handler

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"repos/switchmap/helpers"
	"repos/switchmap/server"
	"sort"
	"strconv"
	"text/template"

	"github.com/gorilla/mux"
)

var (
	data  helpers.ViewData
	build helpers.Build
	floor helpers.Floor
	sw    helpers.Switch
)

//SavePos saves position of switch in db
func SavePos(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	vars := mux.Vars(r)
	swit := vars["switch"]

	top := r.FormValue("top")
	left := r.FormValue("left")

	_, err := server.Core.DB1.Exec("UPDATE `host` set postop = ?, posleft = ? WHERE name = ?", top, left, swit)
	if err != nil {
		log.Printf("Error updating position of switch %s: %s", swit, err)
	}

	w.Write([]byte(top + left))
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
	} else if r.Method == "POST" {
		err := r.ParseForm()
		if err != nil {
			log.Println("Error with parsing data from map page to add build: ", err)
		}

		build.Name = r.FormValue("name")
		build.Address = r.FormValue("address")

		http.Redirect(w, r, "/addbuild", 301)
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
	} else if r.Method == "POST" {
		err := r.ParseForm()
		if err != nil {
			log.Println("Error with parsing data from build page to add floor: ", err)
		}

		floor.Build = build
		floor.Floor = r.FormValue("number")

		http.Redirect(w, r, "/addfloor", 301)
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
	planpath := fmt.Sprintf("static/plans/%s%s.png", build, floor)

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
		} else if r.Method == "POST" {
			err := r.ParseForm()
			if err != nil {
				log.Println("Error with parsing data to add switch on map: ", err)
			}

			sw.Model = r.FormValue("model")
			sw.Name = r.FormValue("name")
			sw.Build = build
			sw.Floor = floor

			http.Redirect(w, r, "/addswitch", 301)
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
