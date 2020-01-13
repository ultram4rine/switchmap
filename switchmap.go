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
	"gopkg.in/alecthomas/kingpin.v2"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
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

	router.PathPrefix("/public/").Handler(http.StripPrefix("/public/", http.FileServer(http.Dir("./public/"))))

	router.PathPrefix("/private/").HandlerFunc(handlers.PrivateHandler)

	router.HandleFunc("/admin/{type}", auth.Handler)

	router.HandleFunc("/map", helpers.AuthCheck(handlers.MapHandler))
	router.HandleFunc("/map/{build}", helpers.AuthCheck(handlers.BuildHandler))
	router.HandleFunc("/map/{build}/{floor}", helpers.AuthCheck(handlers.FloorHandler))

	router.HandleFunc("/vis", helpers.AuthCheck(handlers.VisHandler))
	router.HandleFunc("/getmap", helpers.AsyncCheck(handlers.GetMap)).Methods("GET")

	router.HandleFunc("/swadd", helpers.AsyncCheck(handlers.AddSwitchHandler))
	router.HandleFunc("/badd", helpers.AsyncCheck(handlers.AddBuildHandler))
	router.HandleFunc("/fadd", helpers.AsyncCheck(handlers.AddFloorHandler))

	router.HandleFunc("/savepos", helpers.AsyncCheck(handlers.SavePos))

	router.HandleFunc("/swupdate", helpers.AsyncCheck(handlers.UpdateSwitchHandler))

	router.HandleFunc("/planupdate/{build}/{floor}", helpers.AuthCheck(handlers.PlanUpdateHandler))

	router.HandleFunc("/bdel", helpers.AsyncCheck(handlers.BuildDelHandler))
	router.HandleFunc("/fdel", helpers.AsyncCheck(handlers.FloorDelHandler))
	router.HandleFunc("/swdel", helpers.AsyncCheck(handlers.SwitchDelHandler))

	router.HandleFunc("/list", helpers.AuthCheck(handlers.ListHandler))
	router.HandleFunc("/list/change/{switch}", helpers.AuthCheck(handlers.ChangePage)).Methods("GET")
	router.HandleFunc("/list/change/{switch}", helpers.AsyncCheck(handlers.ChangeHandler)).Methods("POST")

	router.HandleFunc("/logs", helpers.AuthCheck(handlers.LogsHandler))

	router.HandleFunc("/", handlers.RootHandler)

	err = http.ListenAndServe(":"+server.Conf.Server.Port, router)
	if err != nil {
		log.SetOutput(os.Stdout)
		log.Fatal("Error starting server: ", err)
	}
}
