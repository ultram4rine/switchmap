package main

import (
	_ "image/png"
	"log"
	"net/http"
	"os"
	"repos/switchmap/auth"
	"repos/switchmap/handler"
	"repos/switchmap/helpers"
	"repos/switchmap/server"

	_ "github.com/go-sql-driver/mysql"

	"github.com/gorilla/mux"
)

func main() {
	var (
		logFile = "static/log/logs.log"
		port    = ":8080"
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

	err = server.MakeConfig("conf.json")
	if err != nil {
		log.Fatal("Error with making config file to connect to databases: ", err)
	} else {
		log.Println("Config maked!")
	}

	log.Println("Getting started...")
	log.Println("Server listening on " + port + " port")
	log.SetOutput(l)

	server.Connect2DB()
	defer server.Core.DB1.Close()
	defer server.Core.DB2.Close()

	router := mux.NewRouter()

	fs := http.FileServer(http.Dir("./static/"))
	router.PathPrefix("/static/").Handler(http.StripPrefix("/static/", fs))

	router.HandleFunc("/admin/{type}", auth.Handler)

	router.HandleFunc("/map", handler.MapHandler)

	router.HandleFunc("/map/{build}", handler.BuildHandler)

	router.HandleFunc("/map/{build}/{floor}", handler.FloorHandler)

	router.HandleFunc("/savepos/{switch}", handler.SavePos)

	router.HandleFunc("/addswitch", handler.AddSwitchHandler)

	router.HandleFunc("/addbuild", handler.AddBuildHandler)

	router.HandleFunc("/addfloor", handler.AddFloorHandler)

	router.HandleFunc("/planupdate/{build}/{floor}", handler.PlanUpdateHandler)

	router.HandleFunc("/del/{build}", handler.BuildDelHandler)

	router.HandleFunc("/del/{build}/{floor}", handler.FloorDelHandler)

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
