package handlers

import (
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/server"
)

// DeleteSwitchHandler handles switch deleting.
func DeleteSwitchHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM switches WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting switch %s: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Switch %s deleted successfully!", name)
}

// DeleteBuildHandler handles build deleting.
func DeleteBuildHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBdst.Exec("DELETE FROM buildings WHERE name = $1", name)
	if err != nil {
		log.Printf("Error deleting build %s: %s", name, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	log.Printf("Build %s deleted successfully!", name)

}

// DeleteFloorHandler handles floor deleting.
func DeleteFloorHandler(w http.ResponseWriter, r *http.Request) {
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
