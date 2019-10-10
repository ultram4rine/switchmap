package server

import (
	"encoding/json"
	"errors"
	"io/ioutil"
	"log"

	"github.com/gorilla/sessions"
	"github.com/jmoiron/sqlx"
)

//Core is a struct to store important things
var Core struct {
	DBswitchmap *sqlx.DB
	DBnetmap    *sqlx.DB
	Store       *sessions.CookieStore
}

//Conf is a configuration file
var Conf struct {
	DBHost  string `json:"dbHost"`
	DBPort  string `json:"dbPort"`
	DBName  string `json:"dbName"`
	DBLogin string `json:"dbUser"`
	DBPass  string `json:"dbPass"`

	MysqlLogin    string `json:"mysqlLogin"`
	MysqlPassword string `json:"mysqlPassword"`
	MysqlHost     string `json:"mysqlHost"`
	MysqlDb       string `json:"mysqlDb"`

	LdapUser     string `json:"ldapUser"`
	LdapPassword string `json:"ldapPassword"`
	LdapServer   string `json:"ldapServer"`
	LdapBaseDN   string `json:"ldapBaseDN"`

	ListenPort string `json:"listenPort"`
	SessionKey string `json:"sessionKey"`
	EncryptKey string `json:"encryptKey"`
}

//MakeConfig unmarhsal data from JSON Config file
func MakeConfig(filepath string) error {
	confdata, err := ioutil.ReadFile(filepath)
	if err != nil {
		return err
	}

	err = json.Unmarshal(confdata, &Conf)
	if err != nil {
		return err
	}

	return nil
}

func CreateCookieStore() error {
	if Conf.SessionKey == "" {
		return errors.New("Empty session key")
	}

	Core.Store = sessions.NewCookieStore([]byte(Conf.SessionKey), []byte(Conf.EncryptKey))

	return nil
}

//Connect2DB to connect to databases
func Connect2DB() {
	var err error

	Core.DBswitchmap, err = sqlx.Connect("postgres", "user="+Conf.DBLogin+" password="+Conf.DBPass+" host="+Conf.DBHost+" port="+Conf.DBPort+" dbname="+Conf.DBName)
	if err != nil {
		log.Println("Error connecting to switchmap database: ", err)
	} else {
		log.Println("Connected to switchmap database")
	}

	Core.DBnetmap, err = sqlx.Connect("mysql", Conf.MysqlLogin+":"+Conf.MysqlPassword+"@tcp("+Conf.MysqlHost+")/"+Conf.MysqlDb+"?charset=utf8")
	if err != nil {
		log.Println("Error connecting to netmap database: ", err)
	} else {
		log.Println("Connected to netmap database")
	}
}
