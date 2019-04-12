package handler

import (
	"image"
	"io"
	"log"
	"net/http"
	"os"
	"repos/switchmap/helpers"
	"repos/switchmap/server"
	"strconv"
	"text/template"

	"github.com/gorilla/mux"
)

//AddSwitchHandler handle page with host add
func AddSwitchHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	dbsearch, err := server.Core.DB2.Query("SELECT `ip`, `mac`, `switch_id` FROM `host` WHERE `name` = ? AND ip IS NOT NULL", sw.Name)
	if err != nil {
		log.Println("Error with making database query to find IP and MAC of switch: ", err)
	}
	defer dbsearch.Close()

	var (
		IP       string
		MAC      string
		upswitch string
	)

	for dbsearch.Next() {
		err := dbsearch.Scan(&IP, &MAC, &upswitch)
		if err != nil {
			log.Println("Error with scanning database for query: ", err)
		}

		if IP != "" && MAC != "" {
			log.Printf("Switch %s founded!", sw.Name)

			//Searching upswitch name
			if upswitch != "" {
				upswitchsearch, err := server.Core.DB2.Query("SELECT `name` FROM `host` WHERE ip IS NOT NULL AND `id` = ?", upswitch)
				if err != nil {
					log.Println("Error database query for searching upswitch: ", err)
				}
				defer upswitchsearch.Close()

				var upswitchname string

				for upswitchsearch.Next() {
					err := upswitchsearch.Scan(&upswitchname)
					if err != nil {
						log.Println("Error with searching upswitch name: ", err)
					}

					sw.Upswitch = upswitchname
				}

				err = upswitchsearch.Err()
				if err != nil {
					log.Println("Upswitch searching error: ", err)
				}
			} else {
				log.Printf("Is no upswitch for %s in database", sw.Name)
			}
			//End searching upswitch name

			sw.IP = IP
			sw.MAC = MAC
		}
	}
	err = dbsearch.Err()
	if err != nil {
		log.Println("Error searching IP and MAC: ", err)
	}

	if sw.IP != "" && sw.MAC != "" {
		dbwrite, err := server.Core.DB1.Query("SELECT `name` FROM `host` WHERE `name` = ?", sw.Name)
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
			_, err = server.Core.DB1.Exec("UPDATE `host` set ip = ?, mac = ?, model = ?, build = ?, floor = ?, upswitch = ? where name = ?", sw.IP, sw.MAC, sw.Model, sw.Build, sw.Floor, sw.Upswitch, sw.Name)
			if err != nil {
				log.Println("Error updating switch in database: ", err)
			} else {
				log.Printf("Switch %s in %s %s updated successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
			}
		} else {
			reader, err := os.Open("static/plans/" + sw.Build + sw.Floor + ".png")
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

			_, err = server.Core.DB1.Exec("INSERT into `host` (name, ip, mac, revision, serial, model, build, floor, upswitch, postop, posleft) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", sw.Name, sw.IP, sw.MAC, sw.Revision, sw.Serial, sw.Model, sw.Build, sw.Floor, sw.Upswitch, postop, posleft)
			if err != nil {
				log.Println("Error adding switch into database: ", err)
			} else {
				log.Printf("Switch %s in %s %s added successfully! IP: %s, MAC: %s, Serial number: %s", sw.Name, sw.Build, sw.Floor, sw.IP, sw.MAC, sw.Serial)
			}
		}
	} else {
		log.Printf("Can't find switch with that name %s in netmap database", sw.Name)
	}

	http.Redirect(w, r, "/map/"+sw.Build+"/"+sw.Floor, 301)

	sw.Name = ""
	sw.Model = ""
	sw.IP = ""
	sw.MAC = ""
	sw.Revision = ""
	sw.Serial = ""
	sw.Build = ""
	sw.Floor = ""
	sw.Upswitch = ""
}

//AddBuildHandler handle page to add build
func AddBuildHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	dbsearch, err := server.Core.DB1.Query("SELECT `name` from buildings WHERE `name` = ?", build.Name)
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
		_, err = server.Core.DB1.Exec("UPDATE `buildings` set addr = ? WHERE name = ?", build.Address, build.Name)

		log.Printf("Build %s updated successfully! His address: %s", build.Name, build.Address)
	} else {
		_, err = server.Core.DB1.Exec("INSERT into `buildings` (name, addr) values (?, ?)", build.Name, build.Address)

		log.Printf("Build %s added successfully! His address: %s", build.Name, build.Address)
	}

	http.Redirect(w, r, "/map", 301)

	build.Name = ""
	build.Address = ""
}

//AddFloorHandler handle page to add floor
func AddFloorHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	dbsearch, err := server.Core.DB1.Query("SELECT `floor` from floors WHERE `build` = ? AND `floor` = ?", floor.Build, floor.Floor)
	if err != nil {
		log.Println("Error with scanning database to check record of floor: ", err)
	}
	defer dbsearch.Close()

	var floornum = ""
	for dbsearch.Next() {
		err := dbsearch.Scan(&floornum)
		if err != nil {
			log.Println("Error with searching floor in database: ", err)
		}
	}

	if floornum != "" {
		log.Printf("%s floor in build %s already exists", floor.Floor, floor.Build)
	} else {
		_, err = server.Core.DB1.Exec("INSERT into `floors` (build, floor) values (?, ?)", floor.Build, floor.Floor)

		log.Printf("%s floor in build %s added successfully!", floor.Floor, floor.Build)
	}

	http.Redirect(w, r, "/map/"+floor.Build, 301)

	floor.Build = ""
	floor.Floor = ""
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

		f, err := os.OpenFile("static/plans/"+data.Build+data.Floor+".png", os.O_WRONLY|os.O_CREATE, 0666)
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
