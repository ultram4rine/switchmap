package handlers

import (
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/server"
)

// SwitchDelHandler handles switch deleting.
func SwitchDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM switches WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting switch %s: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Switch %s deleted successfully!", name)
}

// BuildDelHandler handles build deleting.
func BuildDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM buildings WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting build %s: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Build %s deleted successfully!", name)

}

// FloorDelHandler handles floor deleting.
func FloorDelHandler(w http.ResponseWriter, r *http.Request) {
	build := r.FormValue("build")
	num := r.FormValue("num")

	_, err := server.Core.DBdst.Exec("DELETE FROM floors WHERE build = $1 AND floor = $2", build, num)
	if err != nil {
		log.Printf("Error deleting floor %s in %s: %s", num, build, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Floor %s in %s deleted successfully!", num, build)
}