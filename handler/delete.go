package handler

import (
	"log"
	"net/http"
	"repos/switchmap/helpers"
	"repos/switchmap/server"

	"github.com/gorilla/mux"
)

//SwitchDelHandler deletes switch
func SwitchDelHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	vars := mux.Vars(r)
	sw := vars["switch"]

	_, err := server.Core.DB1.Exec("DELETE from host WHERE name = ?", sw)
	if err != nil {
		log.Printf("Error deleting switch %s: %s", sw, err)
	} else {
		log.Printf("Switch %s deleted successfully!", sw)
	}

	http.Redirect(w, r, "/list", 301)
}

//BuildDelHandler deletes build
func BuildDelHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	vars := mux.Vars(r)
	build := vars["build"]

	_, err := server.Core.DB1.Exec("UPDATE `buildings` set hidden = ? WHERE addr = ?", 1, build)
	if err != nil {
		log.Printf("Error deleting build %s: %s", build, err)
	} else {
		log.Printf("Build %s deleted successfully!", build)
	}

	http.Redirect(w, r, "/map", 301)
}

//FloorDelHandler deletes floor of build
func FloorDelHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	vars := mux.Vars(r)
	build := vars["build"]
	floor := vars["floor"]

	_, err := server.Core.DB1.Exec("UPDATE `floors` set hidden = ? WHERE `build` = ? AND `floor` = ?", 1, build, floor[1:])
	if err != nil {
		log.Printf("Error deleting floor %s in %s: %s", floor, build, err)
	} else {
		log.Printf("Floor %s in %s deleted successfully!", floor, build)
	}

	http.Redirect(w, r, "/map/"+build, 301)
}
