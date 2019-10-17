package handlers

import (
	"database/sql"
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

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetSwData(sw.Name)
	if err != nil {
		log.Printf("Error getting switch data: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	err = server.Core.DBswitchmap.Get(sw, "SELECT name FROM switches WHERE name = $1", sw.Name)
	if err != nil {
		log.Printf("Error with scanning database to check record of switch: %s", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
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

		sw.Revision, sw.Serial, err = helpers.GetSerial(sw.IP, sw.Model)
		if err != nil {
			log.Printf("Error getting serial number and revision of %s switch: %s", sw.Name, err)
		}

		_, err = server.Core.DBswitchmap.Exec("INSERT INTO switches (name, ip, mac, revision, serial, model, build, floor, upswitch, postop, posleft) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)", sw.Name, sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Postop, sw.Posleft)
		if err != nil {
			log.Printf("Error adding %s switch into database: %s", sw.Name, err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		log.Printf("Switch %s in %s %s added successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
		return
	}

	sw.Revision, sw.Serial, err = helpers.GetSerial(sw.IP, sw.Model)
	if err != nil {
		log.Printf("Error getting serial number and revision of %s switch: %s", sw.Name, err)
	}

	_, err = server.Core.DBswitchmap.Exec("UPDATE switches SET (ip, mac, revision, serial, model, build, floor, upswitch) = ($1, $2, $3, $4, $5, $6, $7, $8) WHERE name = $9", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Name)
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
	name := r.FormValue("name")
	addr := r.FormValue("addr")

	_, err := server.Core.DBswitchmap.Exec("INSERT INTO buildings (name, addr) VALUES ($1, $2)", name, addr)
	if err != nil {
		log.Printf("Error adding build %s to database: %s", name, err)
	}

	log.Printf("Build %s added successfully! His address: %s", name, addr)
}

//AddFloorHandler handle page to add floor
func AddFloorHandler(w http.ResponseWriter, r *http.Request) {
	build := r.FormValue("build")
	num := r.FormValue("num")

	_, err := server.Core.DBswitchmap.Exec("INSERT INTO floors (build, floor) VALUES ($1, $2)", build, num)
	if err != nil {
		log.Printf("Error adding floor %s in %s build to database: %s", num, build, err)
	}

	log.Printf("%s floor in build %s added successfully!", num, build)
}

//ReloadHandler to update data of switch
func ReloadHandler(w http.ResponseWriter, r *http.Request) {
	var (
		sw  helpers.Switch
		err error
	)

	sw.Name = r.FormValue("name")

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetSwData(sw.Name)
	if err != nil {
		log.Println("Error getting switch data: ", err)
	}

	if sw.IP != "" && sw.MAC != "" {
		dbwrite, err := server.Core.DBswitchmap.Query("SELECT name FROM switches WHERE name = $1", sw.Name)
		if err != nil {
			log.Println("Error with scanning database to check record of switch: ", err)
		}
		defer dbwrite.Close()

		var switchname = ""
		for dbwrite.Next() {
			err := dbwrite.Scan(&switchname)
			if err != nil {
				log.Println("Error with searching switch name in database: ", err)
			}
		}

		if switchname != "" {
			sw.Revision, sw.Serial, err = helpers.GetSerial(sw.IP, sw.Model)
			if err != nil {
				log.Println("Error getting serial number: ", err)
			}

			_, err = server.Core.DBswitchmap.Exec("UPDATE switches SET (ip, mac, revision, serial, upswitch) = ($1, $2, $3, $4, $5) where name = $6", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Upswitch, sw.Name)
			if err != nil {
				log.Println("Error updating switch in database: ", err)
			} else {
				log.Printf("Switch %s updated successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.IP, sw.MAC, sw.Serial)
			}
		} else {
			log.Println("No switch with that name in database", err)
		}
	} else {
		log.Printf("Can't find switch with that name %s in netmap database", sw.Name)
	}
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
