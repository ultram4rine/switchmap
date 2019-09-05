package main

import (
	_ "image/png"
	"log"
	"net/http"
	"os"

	"github.com/ultram4rine/switchmap/auth"
	"github.com/ultram4rine/switchmap/handler"
	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	_ "github.com/go-sql-driver/mysql"

	"github.com/gorilla/mux"
)

func main() {
	var (
		confFile = "conf.json"
		logFile  = "private/log/logs.log"
		port     = ":8080"
	)

	if _, err := os.Stat(logFile); err != nil {
		if os.IsNotExist(err) {
			fo, err := os.Create(logFile)
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

	err = server.MakeConfig(confFile)
	if err != nil {
		log.Fatal("Error with making config file to connect to databases: ", err)
	} else {
		log.Println("Config maked!")
	}

	log.Println("Getting started...")
	log.Println("Server listening on " + port + " port")
	log.SetOutput(l)

	server.Connect2DB()
	defer server.Core.DBswitchmap.Close()
	defer server.Core.DBnetmap.Close()

	router := mux.NewRouter()

	router.PathPrefix("/public/").Handler(http.StripPrefix("/public/", http.FileServer(http.Dir("./public/"))))

	router.PathPrefix("/private/").HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !helpers.AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", 302)
			return
		}

		realHandler := http.StripPrefix("/private/", http.FileServer(http.Dir("./private/"))).ServeHTTP
		realHandler(w, r)
	})

	router.HandleFunc("/admin/{type}", auth.Handler)

	router.HandleFunc("/map", handler.MapHandler)

	router.HandleFunc("/map/{build}", handler.BuildHandler)

	router.HandleFunc("/map/{build}/{floor}", handler.FloorHandler)

	router.HandleFunc("/getmap", handler.GetMap)

	router.HandleFunc("/vis", handler.VisHandler)

	router.HandleFunc("/savepos", handler.SavePos)

	router.HandleFunc("/swadd", handler.AddSwitchHandler)

	router.HandleFunc("/badd", handler.AddBuildHandler)

	router.HandleFunc("/fadd", handler.AddFloorHandler)

	router.HandleFunc("/reload", handler.ReloadHandler)

	router.HandleFunc("/planupdate/{build}/{floor}", handler.PlanUpdateHandler)

	router.HandleFunc("/bdel", handler.BuildDelHandler)

	router.HandleFunc("/fdel", handler.FloorDelHandler)

	router.HandleFunc("/swdel", handler.SwitchDelHandler)

	router.HandleFunc("/list", handler.ListHandler)

	router.HandleFunc("/list/change/{switch}", handler.ChangePage).Methods("GET")

	router.HandleFunc("/list/change/{switch}", handler.ChangeHandler).Methods("POST")

	router.HandleFunc("/logs", handler.LogsHandler)

	router.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if !helpers.AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", 301)
		} else {
			http.Redirect(w, r, "/map", 301)
		}
	})

	http.Handle("/", router)

	err = http.ListenAndServe(port, router)
	if err != nil {
		log.SetOutput(os.Stdout)
		log.Fatal("Error starting server: ", err)
	}
}
