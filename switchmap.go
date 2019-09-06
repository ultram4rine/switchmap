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

	_ "github.com/cockroachdb/cockroach-go/crdb"
	_ "github.com/go-sql-driver/mysql"

	"github.com/gorilla/mux"
)

func main() {
	var (
		confFile = "conf.json"
		logFile  = "private/log/logs.log"
	)

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

	err = server.MakeConfig(confFile)
	if err != nil {
		log.Fatal("Error with making config file to connect to databases: ", err)
	} else {
		log.Println("Config maked!")
	}

	log.Println("Getting started...")
	log.Println("Server listening on " + server.Conf.ListenPort + " port")
	log.SetOutput(l)

	server.Connect2DB()
	defer server.Core.DBswitchmap.Close()
	defer server.Core.DBnetmap.Close()

	router := mux.NewRouter()

	router.PathPrefix("/public/").Handler(http.StripPrefix("/public/", http.FileServer(http.Dir("./public/"))))

	router.PathPrefix("/private/").HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !helpers.AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusFound)
			return
		}

		realHandler := http.StripPrefix("/private/", http.FileServer(http.Dir("./private/"))).ServeHTTP
		realHandler(w, r)
	})

	router.HandleFunc("/admin/{type}", auth.Handler)

	router.HandleFunc("/map", helpers.AuthCheck(handlers.MapHandler))

	router.HandleFunc("/map/{build}", helpers.AuthCheck(handlers.BuildHandler))

	router.HandleFunc("/map/{build}/{floor}", helpers.AuthCheck(handlers.FloorHandler))

	router.HandleFunc("/getmap", helpers.AuthCheck(handlers.GetMap))

	router.HandleFunc("/vis", helpers.AuthCheck(handlers.VisHandler))

	router.HandleFunc("/savepos", helpers.AuthCheck(handlers.SavePos))

	router.HandleFunc("/swadd", helpers.AuthCheck(handlers.AddSwitchHandler))

	router.HandleFunc("/badd", helpers.AuthCheck(handlers.AddBuildHandler))

	router.HandleFunc("/fadd", helpers.AuthCheck(handlers.AddFloorHandler))

	router.HandleFunc("/reload", helpers.AuthCheck(handlers.ReloadHandler))

	router.HandleFunc("/planupdate/{build}/{floor}", helpers.AuthCheck(handlers.PlanUpdateHandler))

	router.HandleFunc("/bdel", helpers.AuthCheck(handlers.BuildDelHandler))

	router.HandleFunc("/fdel", helpers.AuthCheck(handlers.FloorDelHandler))

	router.HandleFunc("/swdel", helpers.AuthCheck(handlers.SwitchDelHandler))

	router.HandleFunc("/list", helpers.AuthCheck(handlers.ListHandler))

	router.HandleFunc("/list/change/{switch}", helpers.AuthCheck(handlers.ChangePage)).Methods("GET")

	router.HandleFunc("/list/change/{switch}", helpers.AuthCheck(handlers.ChangeHandler)).Methods("POST")

	router.HandleFunc("/logs", helpers.AuthCheck(handlers.LogsHandler))

	router.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if !helpers.AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusFound)
		} else {
			http.Redirect(w, r, "/map", http.StatusFound)
		}
	})

	err = http.ListenAndServe(":"+server.Conf.ListenPort, router)
	if err != nil {
		log.SetOutput(os.Stdout)
		log.Fatal("Error starting server: ", err)
	}
}
