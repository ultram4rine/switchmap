package server

import (
	"database/sql"
	"encoding/json"
	"errors"
	"io/ioutil"
	"log"

	"github.com/gorilla/sessions"
)

//Core is a struct to store important things
var Core struct {
	DB1   *sql.DB
	DB2   *sql.DB
	Store *sessions.CookieStore
}

//Config is a configuration file
var Config struct {
	MysqlLogin    string `json:"mysqlLogin"`
	MysqlPassword string `json:"mysqlPassword"`
	MysqlHost     string `json:"mysqlHost"`
	MysqlDb       string `json:"mysqlDb"`
	LdapUser      string `json:"ldapUser"`
	LdapPassword  string `json:"ldapPassword"`
	LdapServer    string `json:"ldapServer"`
	LdapBaseDN    string `json:"ldapBaseDN"`
	SessionKey    string `json:"sessionKey"`
}

//MakeConfig unmarhsal data from JSON Config file
func MakeConfig(filepath string) error {
	confdata, err := ioutil.ReadFile(filepath)
	if err != nil {
		return err
	}

	err = json.Unmarshal(confdata, &Config)
	if err != nil {
		return err
	}

	if Config.SessionKey == "" {
		return errors.New("Empty session key")
	}
	Core.Store = sessions.NewCookieStore([]byte(Config.SessionKey))

	return nil
}

//Connect2DB to connect to databases
func Connect2DB() {
	var err error

	Core.DB1, err = sql.Open("mysql", "login:password@tcp(address:port)/db?charset=utf8")
	if err != nil {
		log.Println("Error connecting to database: ", err)
	} else {
		log.Println("Connected to database")
	}

	Core.DB2, err = sql.Open("mysql", "login:password@tcp(address:port)/db?charset=utf8")
	if err != nil {
		log.Println("Error connecting to second database: ", err)
	} else {
		log.Println("Connected to second database")
	}
}
