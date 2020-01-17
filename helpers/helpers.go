package helpers

import (
	"database/sql"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strconv"

	"github.com/ultram4rine/switchmap/server"

	snmp "github.com/soniah/gosnmp"
)

// Build struct describing build.
type Build struct {
	Name    string `db:"name"`
	Address string `db:"addr"`
}

// Floor struct describing floor.
type Floor struct {
	Build string `db:"build"`
	Floor string `db:"floor"`
}

// Switch struct describing switch.
type Switch struct {
	Name     string `db:"name"`
	IP       string `db:"ip"`
	MAC      string `db:"mac"`
	Revision string `db:"revision"`
	Serial   string `db:"serial"`
	Model    string `db:"model"`
	Build    string `db:"build"`
	Floor    string `db:"floor"`
	Upswitch string `db:"upswitch"`
	Port     string `db:"port"`
	Postop   string `db:"postop"`
	Posleft  string `db:"posleft"`
}

// ViewData struct for data that inserts into HTML templates.
type ViewData struct {
	Build  string      // Build to go back from build page.
	Floor  string      // Floor to go back from plan page.
	User   interface{} // User to show username of user.
	Sw     Switch      // Sw to show switch information on change page.
	Swits  []Switch    // Swits to show information of switch on plan.
	Builds []Build     // Builds to show build on map.
	Floors []Floor     // Floors to show floors in build.
}

// GetMainSwData gets IP, MAC and UpSwitchName of switch from source database.
func GetMainSwData(name string) (ip, mac, upswitchname, port string, err error) {
	type netmapSwitch struct {
		Name         string         `db:"name"`
		IP           string         `db:"ip"`
		MAC          string         `db:"mac"`
		UpSwitchID   sql.NullString `db:"switch_id"`
		UpSwitchName string
		Port         sql.NullString `db:"port"`
	}

	var sw netmapSwitch

	err = server.Core.DBsrc.Get(&sw, "SELECT ip, mac, switch_id, port FROM unetmap_host WHERE name = ? AND ip IS NOT NULL", name)
	if err == sql.ErrNoRows {
		return "", "", "", "", fmt.Errorf("can't find switch with %s name", name)
	}
	if err != nil {
		log.Printf("Error getting IP, MAC and UpSwitchID for %s switch from netmap database: %s", name, err)
		return "", "", "", "", err
	}

	if sw.UpSwitchID.Valid {
		var upswitch netmapSwitch

		err = server.Core.DBsrc.Get(&upswitch, "SELECT name FROM unetmap_host WHERE ip IS NOT NULL AND id = ?", sw.UpSwitchID)
		if err == sql.ErrNoRows {
			log.Printf("Can't find UpSwitchName for %s switch by %s UpSwitchID in netmap database", name, sw.UpSwitchID.String)
		} else if err != nil {
			log.Printf("Error getting UpSwitchName for %s switch from netmap database: %s", name, err)
		}

		sw.UpSwitchName = upswitch.Name
	} else {
		log.Printf("Here no upswitch for %s switch", name)
	}

	intIP, err := strconv.Atoi(sw.IP)
	if err != nil {
		log.Printf("Error converting string IP to int IP: %s", err)
		return "", "", "", "", err
	}

	realIP := fmt.Sprintf("%d.%d.%d.%d", byte(intIP>>24), byte(intIP>>16), byte(intIP>>8), byte(intIP))

	return realIP, sw.MAC, sw.UpSwitchName, sw.Port.String, nil
}

const (
	entPhysicalDescr     = ".1.3.6.1.2.1.47.1.1.1.1.2.1"
	entPhysicalSerialNum = ".1.3.6.1.2.1.47.1.1.1.1.11.1"
)

// GetAdditionalSwData trying to get serial number and revision of switch by SNMP.
func GetAdditionalSwData(ip, model string) (rev, sernum string, err error) {
	if model == "D-Link" {
		return "", "", errors.New("can't use snmp on d-link to get serial number")
	}

	snmp.Default.Target = ip
	err = snmp.Default.Connect()
	if err != nil {
		return "", "", err
	}
	defer snmp.Default.Conn.Close()

	oids := []string{entPhysicalDescr, entPhysicalSerialNum}

	result, err := snmp.Default.Get(oids)
	if err != nil {
		return "", "", err
	}

	for _, v := range result.Variables {
		switch v.Type {
		case snmp.OctetString:
			switch v.Name {
			case entPhysicalDescr:
				rev = string(v.Value.([]byte))
			case entPhysicalSerialNum:
				sernum = string(v.Value.([]byte))
			}
		default:
			return "", "", errors.New("can't get serial number")
		}
	}

	return rev, sernum, nil
}

// AlreadyLogin checks is user already logged in.
func AlreadyLogin(r *http.Request) bool {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
		return false
	}

	return session.Values["user"] != nil
}

// AuthCheck is a middleware for handlers.
func AuthCheck(handler http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if !AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusFound)
			return
		}

		handler(w, r)
	}
}

// AsyncCheck is a middleware for async requests.
func AsyncCheck(handler http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if !AlreadyLogin(r) {
			http.Error(w, "You're not authorized", http.StatusUnauthorized)
			return
		}

		handler(w, r)
	}
}
