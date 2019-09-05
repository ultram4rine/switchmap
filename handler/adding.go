package handler

import (
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
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	sw.Name = r.FormValue("name")
	sw.Model = r.FormValue("model")
	sw.Build = r.FormValue("build")
	sw.Floor = r.FormValue("floor")

	var err error

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetSwData(sw.Name)
	if err != nil {
		log.Println("Error getting switch data: ", err)
	}

	if sw.IP != "" && sw.MAC != "" {
		dbwrite, err := server.Core.DBswitchmap.Query("SELECT `name` FROM `host` WHERE `name` = ?", sw.Name)
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

			_, err = server.Core.DBswitchmap.Exec("UPDATE `host` set ip = ?, mac = ?, revision = ?, serial = ?, model = ?, build = ?, floor = ?, upswitch = ? where name = ?", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Name)
			if err != nil {
				log.Println("Error updating switch in database: ", err)
			} else {
				log.Printf("Switch %s in %s %s updated successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
			}
		} else {
			reader, err := os.Open("private/plans/" + sw.Build + sw.Floor + ".png")
			if err != nil {
				log.Println("Error opening plan image to count size: ", err)
			}
			defer reader.Close()

			im, _, err := image.DecodeConfig(reader)
			if err != nil {
				log.Println("Error decoding plan image to count size: ", err)
			}

			postop := strconv.Itoa(im.Height / 2)
			posleft := strconv.Itoa(im.Width / 2)

			sw.Revision, sw.Serial, err = helpers.GetSerial(sw.IP, sw.Model)
			if err != nil {
				log.Println("Error getting serial number: ", err)
			}

			_, err = server.Core.DBswitchmap.Exec("INSERT into `host` (name, ip, mac, revision, serial, model, build, floor, upswitch, postop, posleft) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", sw.Name, sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, postop, posleft)
			if err != nil {
				log.Println("Error adding switch into database: ", err)
			} else {
				log.Printf("Switch %s in %s %s added successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
			}
		}
	} else {
		log.Printf("Can't find switch with that name %s in netmap database", sw.Name)
	}
}

//AddBuildHandler handle page to add build
func AddBuildHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	name := r.FormValue("name")
	addr := r.FormValue("addr")

	dbsearch, err := server.Core.DBswitchmap.Query("SELECT `name` from buildings WHERE `name` = ?", name)
	if err != nil {
		log.Println("Error with scanning database to check record of build: ", err)
	}
	defer dbsearch.Close()

	var buildname = ""
	for dbsearch.Next() {
		err := dbsearch.Scan(&buildname)
		if err != nil {
			log.Println("Error with searching build name in database: ", err)
		}
	}

	if buildname != "" {
		_, err = server.Core.DBswitchmap.Exec("UPDATE `buildings` set addr = ?, hidden = ? WHERE name = ?", addr, 0, name)

		log.Printf("Build %s updated successfully! His address: %s", name, addr)
	} else {
		_, err = server.Core.DBswitchmap.Exec("INSERT into `buildings` (name, addr, hidden) values (?, ?, ?)", name, addr, 0)

		log.Printf("Build %s added successfully! His address: %s", name, addr)
	}
}

//AddFloorHandler handle page to add floor
func AddFloorHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	build := r.FormValue("build")
	num := r.FormValue("num")

	dbsearch, err := server.Core.DBswitchmap.Query("SELECT `floor`, `hidden` from floors WHERE `build` = ? AND `floor` = ?", build, num)
	if err != nil {
		log.Println("Error with scanning database to check record of floor: ", err)
	}
	defer dbsearch.Close()

	var (
		floornum = ""
		hidden   uint
	)
	for dbsearch.Next() {
		err := dbsearch.Scan(&floornum, &hidden)
		if err != nil {
			log.Println("Error with searching floor in switchmap database: ", err)
		}
	}

	if floornum != "" {
		if hidden == 1 {
			_, err = server.Core.DBswitchmap.Exec("UPDATE `floors` set hidden = ? WHERE `build` = ? AND `floor` = ?", 0, build, num)
		} else {
			log.Printf("%s floor in build %s already exists", num, build)
		}
	} else {
		_, err = server.Core.DBswitchmap.Exec("INSERT into `floors` (build, floor, hidden) values (?, ?, ?)", build, num, 0)

		log.Printf("%s floor in build %s added successfully!", num, build)
	}
}

//ReloadHandler to update data of switch
func ReloadHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	sw.Name = r.FormValue("name")

	var err error

	sw.IP, sw.MAC, sw.Upswitch, err = helpers.GetSwData(sw.Name)
	if err != nil {
		log.Println("Error getting switch data: ", err)
	}

	if sw.IP != "" && sw.MAC != "" {
		dbwrite, err := server.Core.DBswitchmap.Query("SELECT `name` FROM `host` WHERE `name` = ?", sw.Name)
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

			_, err = server.Core.DBswitchmap.Exec("UPDATE `host` set ip = ?, mac = ?, revision = ?, serial = ?, upswitch = ? where name = ?", sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Upswitch, sw.Name)
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
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}
	session, _ := server.Core.Store.Get(r, "session")

	vars := mux.Vars(r)
	build := vars["build"]
	floor := vars["floor"]

	data = helpers.ViewData{
		Build: build,
		Floor: floor,
		User:  session.Values["user"],
	}
	if r.Method == "GET" {
		tmpl, _ := template.ParseFiles("templates/upload.html")
		tmpl.Execute(w, data)
	} else if r.Method == "POST" {
		r.ParseMultipartForm(32 << 20)
		file, _, err := r.FormFile("load")
		if err != nil {
			log.Println("Error with forming file: ", err)
		}
		defer file.Close()

		f, err := os.OpenFile("private/plans/"+data.Build+data.Floor+".png", os.O_WRONLY|os.O_CREATE, 0666)
		if err != nil {
			log.Println("Error with creating file: ", err)
		}
		defer f.Close()

		_, err = io.Copy(f, file)
		if err != nil {
			log.Println("Error with writing file: ", err)
		}

		http.Redirect(w, r, "/map/"+build+"/"+floor, 301)
	}
}
