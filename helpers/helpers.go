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

//Build is a type describing build
type Build struct {
	Name    string `db:"name"`
	Address string `db:"addr"`
}

//Floor is a type describing floor
type Floor struct {
	Build string `db:"build"`
	Floor string `db:"floor"`
}

//Switch is a type describing switch
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
	Postop   string `db:"postop"`
	Posleft  string `db:"posleft"`
}

//ViewData is a struct describing data that inserts into HTML files
type ViewData struct {
	Build  string      //Build to go back from build page
	Floor  string      //Floor to go back from plan page
	User   interface{} //User to show username of user
	Sw     Switch      //Sw to show switch information on change page
	Swits  []Switch    //Swits to show information of switch on plan
	Builds []Build     //Builds to show build on map
	Floors []Floor     //Floors to show floors in build
}

//GetMainSwData gets IP, MAC and UpSwitchName of switch from netmap database
func GetMainSwData(name string) (ip, mac, upswitchname string, err error) {
	type netmapSwitch struct {
		Name         string         `db:"name"`
		IP           string         `db:"ip"`
		MAC          string         `db:"mac"`
		UpSwitchID   sql.NullString `db:"switch_id"`
		UpSwitchName string
	}

	var sw netmapSwitch

	err = server.Core.DBsrc.Get(&sw, "SELECT ip, mac, switch_id FROM unetmap_host WHERE name = ? AND ip IS NOT NULL", name)
	if err == sql.ErrNoRows {
		return "", "", "", fmt.Errorf("can't find switch with %s name", name)
	}
	if err != nil {
		log.Printf("Error getting IP, MAC and UpSwitchID for %s switch from netmap database: %s", name, err)
		return "", "", "", err
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
		return "", "", "", err
	}

	realIP := fmt.Sprintf("%d.%d.%d.%d", byte(intIP>>24), byte(intIP>>16), byte(intIP>>8), byte(intIP))

	return realIP, sw.MAC, sw.UpSwitchName, nil
}

//GetAdditionalSwData trying to get serial number and revision of switch by SNMP
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

	oid := []string{"1.3.6.1.2.1.47.1.1.1.1.2.1", "1.3.6.1.2.1.47.1.1.1.1.11.1"}

	result, err := snmp.Default.Get(oid)
	if err != nil {
		return "", "", err
	}

	for i, variable := range result.Variables {
		switch variable.Type {
		case snmp.OctetString:
			bytes := variable.Value.([]byte)
			switch i {
			case 0:
				rev = string(bytes)
			case 1:
				sernum = string(bytes)
			}
		default:
			return "", "", errors.New("can't get serial number")
		}
	}

	return rev, sernum, nil
}

func MakeVisMap() (map[string][]string, error) {
	var (
		switches []Switch
		vis      = make(map[string][]string)
	)

	err := server.Core.DBdst.Select(&switches, "SELECT name from switches")
	if err != nil {
		log.Println("Error with making query to show visualization")
		return nil, err
	}

	for _, sw := range switches {
		var downSwitches []Switch

		err = server.Core.DBdst.Select(&downSwitches, "SELECT name FROM switches WHERE upswitch = $1", sw.Name)
		if err != nil {
			log.Printf("Error finding downSwitches of %s switch: %s", sw.Name, err)
		}

		var downSwitchesNames []string

		for _, dSw := range downSwitches {
			downSwitchesNames = append(downSwitchesNames, dSw.Name)
		}

		vis[sw.Name] = downSwitchesNames
	}

	return vis, nil
}

//AlreadyLogin checks is user already logged in
func AlreadyLogin(r *http.Request) bool {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
		return false
	}

	return session.Values["userName"] != nil
}

//AuthCheck is a middleware for handlers
func AuthCheck(handler http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if !AlreadyLogin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusFound)
			return
		}

		handler(w, r)
	}
}
