package helpers

import (
	"net/http"
	"repos/switchmap/server"
)

//Build is a type describing build
type Build struct {
	Name    string
	Address string
}

//Floor is a type describing floor
type Floor struct {
	Build string
	Floor string
}

//Switch is a type describing switch
type Switch struct {
	Model    string
	Name     string
	IP       string
	MAC      string
	Upswitch string
	Build    string
	Floor    string
	Postop   string
	Posleft  string
}

//ViewData is a struct describing data that inserts into HTML files
type ViewData struct {
	Build  string      //Build to go back from build page
	Floor  string      //Floor to go back from plan page
	User   interface{} //User to show username of user
	Swits  []Switch    //Swits to show information of switch on plan
	Builds []Build     //Builds to show build on map
	Floors []Floor     //Floors to show floors in build
}

//AlreadyLogin checks is user already logged in
func AlreadyLogin(r *http.Request) bool {
	session, _ := server.Core.Store.Get(r, "session")
	return session.Values["userName"] != nil
}
