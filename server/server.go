package server

import (
	"errors"
	"fmt"
	"log"

	"github.com/BurntSushi/toml"
	"github.com/go-sql-driver/mysql"
	"github.com/gorilla/sessions"
	"github.com/jmoiron/sqlx"
)

// Core struct stores DB connections and cookie store.
var Core struct {
	DBdst *sqlx.DB
	DBsrc *sqlx.DB
	Store *sessions.CookieStore
}

// Conf struct contains parameters of app.
var Conf struct {
	DBdst  dbConfig     `toml:"db_dst"`
	DBsrc  dbConfig     `toml:"db_src"`
	LDAP   ldapConfig   `toml:"ldap"`
	Server serverConfig `toml:"server"`
}

type dbConfig struct {
	Host string `toml:"host"`
	Port string `toml:"port"`
	Name string `toml:"name"`
	User string `toml:"user"`
	Pass string `toml:"pass"`
}

type ldapConfig struct {
	Server string `toml:"server"`
	User   string `toml:"user"`
	Pass   string `toml:"pass"`
	BaseDN string `toml:"base_dn"`
}

type serverConfig struct {
	Port       string `toml:"port"`
	SessionKey string `toml:"session_key"`
	EncryptKey string `toml:"encrypt_key"`
}

func makeConfig(filepath string) error {
	if _, err := toml.DecodeFile(filepath, &Conf); err != nil {
		return fmt.Errorf("error decoding config: %s", err)
	}
	return nil
}

func createCookieStore() error {
	if Conf.Server.SessionKey == "" {
		return errors.New("empty session key")
	}
	if Conf.Server.EncryptKey == "" {
		return errors.New("empty encryption key")
	}
	Core.Store = sessions.NewCookieStore([]byte(Conf.Server.SessionKey), []byte(Conf.Server.EncryptKey))
	return nil
}

func connect2DB() error {
	var err error

	Core.DBdst, err = sqlx.Connect("postgres", "user="+Conf.DBdst.User+" password="+Conf.DBdst.Pass+" host="+Conf.DBdst.Host+" port="+Conf.DBdst.Port+" dbname="+Conf.DBdst.Name)
	if err != nil {
		return fmt.Errorf("error connecting to switchmap database: %s", err)
	}
	log.Println("Connected to switchmap database")

	dbSrcConf := mysql.NewConfig()
	dbSrcConf.Net = "tcp"
	if Conf.DBsrc.Host == "localhost" || Conf.DBsrc.Host == "127.0.0.1" {
		dbSrcConf.Net = "unixgram"
	}
	dbSrcConf.Addr = Conf.DBsrc.Host + Conf.DBsrc.Port
	dbSrcConf.DBName = Conf.DBsrc.Name
	dbSrcConf.User = Conf.DBsrc.User
	dbSrcConf.Passwd = Conf.DBsrc.Pass
	dbSrcConf.ParseTime = true
	dbSrcConf.MultiStatements = true

	Core.DBsrc, err = sqlx.Connect("mysql", dbSrcConf.FormatDSN())
	if err != nil {
		return fmt.Errorf("error connecting to source database: %s", err)
	}
	log.Println("Connected to source database")

	return nil
}

// Init function initializes server.
func Init(confPath string) error {
	err := makeConfig(confPath)
	if err != nil {
		return err
	}
	log.Println("Config maked")

	err = createCookieStore()
	if err != nil {
		return err
	}
	log.Println("Cookie store created")

	err = connect2DB()
	if err != nil {
		return err
	}

	_, err = sqlx.LoadFile(Core.DBdst, "schema.sql")
	if err != nil {
		return err
	}

	return nil
}
