package handler

import (
	"log"
	"net/http"
	"repos/switchmap/helpers"
	"repos/switchmap/server"
)

//SwitchDelHandler deletes switch
func SwitchDelHandler(w http.ResponseWriter, r *http.Request) {
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	name := r.FormValue("name")

	_, err := server.Core.DB1.Exec("DELETE from host WHERE name = ?", name)
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
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	name := r.FormValue("name")

	_, err := server.Core.DB1.Exec("UPDATE `buildings` set hidden = ? WHERE name = ?", 1, name)
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
	if !helpers.AlreadyLogin(r) {
		http.Redirect(w, r, "/admin/login", 302)
		return
	}

	build := r.FormValue("build")
	num := r.FormValue("num")

	_, err := server.Core.DB1.Exec("UPDATE `floors` set hidden = ? WHERE `build` = ? AND `floor` = ?", 1, build, num)
	if err != nil {
		log.Printf("Error deleting floor %s in %s: %s", num, build, err)
		w.Write([]byte("error"))
	} else {
		log.Printf("Floor %s in %s deleted successfully!", num, build)
		w.Write([]byte("success"))
	}
}
