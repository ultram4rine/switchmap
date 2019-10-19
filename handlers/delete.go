package handlers

import (
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/server"
)

//SwitchDelHandler deletes switch
func SwitchDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM switches WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting switch %s: %s", name, err)

		_, err = w.Write([]byte("error"))
		if err != nil {
			log.Printf("Error writing answer for %s switch deleting(error): %s", name, err)
		}
	} else {
		log.Printf("Switch %s deleted successfully!", name)

		_, err = w.Write([]byte("success"))
		if err != nil {
			log.Printf("Error writing answer for %s switch deleting(success): %s", name, err)
		}
	}
}

//BuildDelHandler deletes build
func BuildDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM buildings WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting build %s: %s", name, err)

		_, err = w.Write([]byte("error"))
		if err != nil {
			log.Printf("Error writing answer for %s build deleting(error): %s", name, err)
		}
	} else {
		log.Printf("Build %s deleted successfully!", name)

		_, err = w.Write([]byte("success"))
		if err != nil {
			log.Printf("Error writing answer for %s build deleting(success): %s", name, err)
		}
	}
}

//FloorDelHandler deletes floor of build
func FloorDelHandler(w http.ResponseWriter, r *http.Request) {
	build := r.FormValue("build")
	num := r.FormValue("num")

	_, err := server.Core.DBdst.Exec("DELETE FROM floors WHERE build = $1 AND floor = $2", 1, build, num)
	if err != nil {
		log.Printf("Error deleting floor %s in %s: %s", num, build, err)

		_, err = w.Write([]byte("error"))
		if err != nil {
			log.Printf("Error writing answer for %s floor in %s build deleting(error): %s", num, build, err)
		}
	} else {
		log.Printf("Floor %s in %s deleted successfully!", num, build)

		_, err = w.Write([]byte("success"))
		if err != nil {
			log.Printf("Error writing answer for %s floor in %s build deleting(success): %s", num, build, err)
		}
	}
}
