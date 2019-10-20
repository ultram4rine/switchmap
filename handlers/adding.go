package handlers

import (
	"database/sql"
	"fmt"
	"image"
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
	"text/template"

	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/gorilla/mux"
)

//AddSwitchHandler handle page with host add
func AddSwitchHandler(w http.ResponseWriter, r *http.Request) {
	var (
		sw  helpers.Switch
		err error
	)

	sw.Name = r.FormValue("name")
	sw.Model = r.FormValue("model")
	sw.Build = r.FormValue("build")
	sw.Floor = r.FormValue("floor")

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetMainSwData(sw.Name)
	if err != nil {
		log.Printf("Error getting switch data: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	sw.Revision, sw.Serial, err = helpers.GetAdditionalSwData(sw.IP, sw.Model)
	if err != nil {
		log.Printf("Error getting serial number and revision of %s switch: %s", sw.Name, err)
	}

	err = server.Core.DBdst.Get(&sw, "SELECT name FROM switches WHERE name = $1", sw.Name)
	if err == sql.ErrNoRows {
		reader, err := os.Open("private/plans/" + sw.Build + sw.Floor + ".png")
		if err != nil {
			log.Printf("Error opening plan image to get image size: %s", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer reader.Close()

		planImage, _, err := image.DecodeConfig(reader)
		if err != nil {
			log.Printf("Error decoding plan image to get image size: %s", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		sw.Postop = strconv.Itoa(planImage.Height / 2)
		sw.Posleft = strconv.Itoa(planImage.Width / 2)

		_, err = server.Core.DBdst.Exec("INSERT INTO switches (name, ip, mac, revision, serial, model, build, floor, upswitch, postop, posleft) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)", sw.Name, sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Postop, sw.Posleft)
		if err != nil {
			log.Printf("Error adding %s switch into database: %s", sw.Name, err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		log.Printf("Switch %s in %s %s added successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
		return
	}
	if err != nil {
		log.Printf("Error with scanning database to check record of switch: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	_, err = server.Core.DBdst.Exec("UPDATE switches SET (ip, mac, revision, serial, model, build, floor, upswitch) = ($1, $2, $3, $4, $5, $6, $7, $8) WHERE name = $9", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Name)
	if err != nil {
		log.Printf("Error updating %s switch into database: %s", sw.Name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Switch %s in %s %s updated successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
	return
}

//AddBuildHandler handle page to add build
func AddBuildHandler(w http.ResponseWriter, r *http.Request) {
	var b helpers.Build

	name := r.FormValue("name")
	addr := r.FormValue("addr")

	err := server.Core.DBdst.Get(&b, "SELECT * from buildings WHERE name = $1 AND addr = $2", name, addr)
	if err == sql.ErrNoRows {
		_, err = server.Core.DBdst.Exec("INSERT INTO buildings (name, addr) VALUES ($1, $2)", name, addr)
		if err != nil {
			log.Printf("Error adding build %s to database: %s", name, err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		log.Printf("Build %s successfully added! His address: %s", name, addr)
		return
	}
	if err != nil {
		log.Printf("Error checking record of %s build with %s address: %s", name, addr, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	http.Error(w, fmt.Sprintf("Build %s with address %s already exists", name, addr), http.StatusInternalServerError)
}

//AddFloorHandler handle page to add floor
func AddFloorHandler(w http.ResponseWriter, r *http.Request) {
	var f helpers.Floor

	build := r.FormValue("build")
	num := r.FormValue("num")

	err := server.Core.DBdst.Get(&f, "SELECT * from floors WHERE build = $1 AND floor = $2", build, num)
	if err == sql.ErrNoRows {
		_, err = server.Core.DBdst.Exec("INSERT INTO floors (build, floor) VALUES ($1, $2)", build, num)
		if err != nil {
			log.Printf("Error adding %s floor in %s build to database: %s", num, build, err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		log.Printf("Floor %s in %s build successfully added!", num, build)
		return
	}
	if err != nil {
		log.Printf("Error checking record of %s floor in %s build: %s", num, build, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	http.Error(w, fmt.Sprintf("Floor %s in %s build already exists", num, build), http.StatusInternalServerError)
}

//UpdateSwitchHandler to update data of switch
func UpdateSwitchHandler(w http.ResponseWriter, r *http.Request) {
	var (
		sw  helpers.Switch
		err error
	)

	name := r.FormValue("name")

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetMainSwData(name)
	if err != nil {
		log.Printf("Error getting %s switch data to update: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	err = server.Core.DBdst.Get(&sw, "SELECT name FROM switches WHERE name = $1", name)
	if err == sql.ErrNoRows {
		http.Error(w, fmt.Sprintf("Here no %s switch to update", name), http.StatusInternalServerError)
		return
	}
	if err != nil {
		log.Printf("Error with scanning database to check record of %s switch to update: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	sw.Revision, sw.Serial, err = helpers.GetAdditionalSwData(sw.IP, sw.Model)
	if err != nil {
		log.Printf("Error getting serial number and revision of %s switch to update: %s", sw.Name, err)
	}

	_, err = server.Core.DBdst.Exec("UPDATE switches SET (ip, mac, revision, serial, upswitch) = ($1, $2, $3, $4, $5) where name = $6", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Upswitch, sw.Name)
	if err != nil {
		log.Printf("Error updating %s switch data: %s", sw.Name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Switch %s data updated successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.IP, sw.MAC, sw.Serial)
	return
}

//PlanUpdateHandler to upload or update floor's plan
func PlanUpdateHandler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	vars := mux.Vars(r)
	build := vars["build"]
	floor := vars["floor"]

	data = helpers.ViewData{
		Build: build,
		Floor: floor,
		User:  session.Values["user"],
	}

	if r.Method == "GET" {
		tmpl, err := template.ParseFiles("templates/upload.html")
		if err != nil {
			log.Printf("Error parsing template files for upload plan page for %s floor in %s build: %s", floor, build, err)
		}

		err = tmpl.Execute(w, data)
		if err != nil {
			log.Printf("Error executing template for upload plan page for %s floor in %s build: %s", floor, build, err)
		}
	} else if r.Method == "POST" {
		err = r.ParseMultipartForm(32 << 20)
		if err != nil {
			log.Printf("Error parsing plan image for %s floor in %s build: %s", floor, build, err)
		}

		file, _, err := r.FormFile("load")
		if err != nil {
			log.Printf("Error getting file from form for %s floor in %s build: %s", floor, build, err)
		}
		defer file.Close()

		f, err := os.OpenFile("private/plans/"+data.Build+data.Floor+".png", os.O_WRONLY|os.O_CREATE, 0666)
		if err != nil {
			log.Printf("Error creating plan image file for %s floor in %s build: %s", floor, build, err)
		}
		defer f.Close()

		_, err = io.Copy(f, file)
		if err != nil {
			log.Printf("Error writing plan image file for %s floor in %s build: %s", floor, build, err)
		}

		http.Redirect(w, r, "/map/"+build+"/"+floor, http.StatusFound)
	}
}
