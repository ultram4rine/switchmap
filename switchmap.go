package main

import (
	_ "image/png"
	"log"
	"net/http"
	"os"

	"github.com/ultram4rine/switchmap/auth"
	"github.com/ultram4rine/switchmap/handlers"
	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
	"gopkg.in/alecthomas/kingpin.v2"
)

var (
	confPath = kingpin.Flag("config", "Path to config file").Short('c').Default("switchmap.conf.toml").String()
	prod     = kingpin.Flag("prod", "Run in production mode").Short('p').Bool()
)

func main() {
	kingpin.Parse()

	var logFile = "private/log/logs.log"

	if _, err := os.Stat(logFile); err != nil {
		if os.IsNotExist(err) {
			fo, err := os.OpenFile("private/log/logs.log", os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
			if err != nil {
				log.Println("Error creating log file: ", err)
			}
			defer fo.Close()
		}
	}

	l, err := os.OpenFile(logFile, os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
	if err != nil {
		log.Println("Error opening log file: ", err)
	}
	defer l.Close()

	err = server.Init(*confPath)
	if err != nil {
		log.Fatalf("Error initializing server: %s", err)
	}

	log.Println("Getting started...")
	log.Println("Server must listen on " + server.Conf.Server.Port + " port")
	log.SetOutput(l)

	defer server.Core.DBdst.Close()
	defer server.Core.DBsrc.Close()

	router := mux.NewRouter()
	setRoutes(router)

	err = http.ListenAndServe(":"+server.Conf.Server.Port, router)
	if err != nil {
		log.SetOutput(os.Stdout)
		log.Fatal("Error starting server: ", err)
	}
}

func setRoutes(r *mux.Router) {
	r.PathPrefix("/public/").Handler(http.StripPrefix("/public/", http.FileServer(http.Dir("./public/"))))

	r.PathPrefix("/private/").HandlerFunc(handlers.PrivateHandler)

	r.HandleFunc("/admin/{type}", auth.Handler)

	r.HandleFunc("/map", helpers.AuthCheck(handlers.MapHandler))
	r.HandleFunc("/map/{build}", helpers.AuthCheck(handlers.BuildHandler))
	r.HandleFunc("/map/{build}/{floor}", helpers.AuthCheck(handlers.FloorHandler))

	r.HandleFunc("/vis", helpers.AuthCheck(handlers.VisHandler))
	r.HandleFunc("/getmap", helpers.AsyncCheck(handlers.GetMap)).Methods("GET")

	r.HandleFunc("/add/build", helpers.AsyncCheck(handlers.AddBuildHandler))
	r.HandleFunc("/add/floor", helpers.AsyncCheck(handlers.AddFloorHandler))
	r.HandleFunc("/add/switch", helpers.AsyncCheck(handlers.AddSwitchHandler))

	r.HandleFunc("/savepos", helpers.AsyncCheck(handlers.SavePos))

	r.HandleFunc("/update/switch", helpers.AsyncCheck(handlers.UpdateSwitchHandler))

	r.HandleFunc("/planupdate/{build}/{floor}", helpers.AuthCheck(handlers.PlanUpdateHandler))

	r.HandleFunc("/delete/build", helpers.AsyncCheck(handlers.DeleteBuildHandler))
	r.HandleFunc("/delete/floor", helpers.AsyncCheck(handlers.DeleteFloorHandler))
	r.HandleFunc("/delete/switch", helpers.AsyncCheck(handlers.DeleteSwitchHandler))

	r.HandleFunc("/list", helpers.AuthCheck(handlers.ListHandler))
	r.HandleFunc("/list/change/{switch}", helpers.AuthCheck(handlers.ChangePage)).Methods("GET")
	r.HandleFunc("/list/change/{switch}", helpers.AsyncCheck(handlers.ChangeHandler)).Methods("POST")

	r.HandleFunc("/logs", helpers.AuthCheck(handlers.LogsHandler))

	r.HandleFunc("/", handlers.RootHandler)
}
