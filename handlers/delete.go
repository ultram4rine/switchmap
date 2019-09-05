package handlers

import (
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/server"
)

//SwitchDelHandler deletes switch
func SwitchDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBswitchmap.Exec("DELETE from host WHERE name = ?", name)
	if err != nil {
		log.Printf("Error deleting switch %s: %s", name, err)
		w.Write([]byte("error"))
	} else {
		log.Printf("Switch %s deleted successfully!", name)
		w.Write([]byte("success"))
	}
}

//BuildDelHandler deletes build
func BuildDelHandler(w http.ResponseWriter, r *http.Request) {
	name := r.FormValue("name")

	_, err := server.Core.DBswitchmap.Exec("UPDATE `buildings` set hidden = ? WHERE name = ?", 1, name)
	if err != nil {
		log.Printf("Error deleting build %s: %s", name, err)
		w.Write([]byte("error"))
	} else {
		log.Printf("Build %s deleted successfully!", name)
		w.Write([]byte("success"))
	}
}

//FloorDelHandler deletes floor of build
func FloorDelHandler(w http.ResponseWriter, r *http.Request) {
	build := r.FormValue("build")
	num := r.FormValue("num")

	_, err := server.Core.DBswitchmap.Exec("UPDATE `floors` set hidden = ? WHERE `build` = ? AND `floor` = ?", 1, build, num)
	if err != nil {
		log.Printf("Error deleting floor %s in %s: %s", num, build, err)
		w.Write([]byte("error"))
	} else {
		log.Printf("Floor %s in %s deleted successfully!", num, build)
		w.Write([]byte("success"))
	}
}
