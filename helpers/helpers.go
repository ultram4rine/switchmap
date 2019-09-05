package helpers

import (
	"database/sql"
	"errors"
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/server"

	snmp "github.com/soniah/gosnmp"
)

//Build is a type describing build
type Build struct {
	Name    string
	Address string
}

//Floor is a type describing floor
type Floor struct {
	Build string
	Floor string
}

//Switch is a type describing switch
type Switch struct {
	Model    string
	Name     string
	IP       string
	MAC      string
	Upswitch string
	Revision string
	Serial   string
	Build    string
	Floor    string
	Postop   string
	Posleft  string
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

//GetSwData gets switch data
func GetSwData(name string) (ip, mac, upswitch string, err error) {
	dbsearch, err := server.Core.DBnetmap.Query("SELECT `ip`, `mac`, `switch_id` FROM `host` WHERE `name` = ? AND ip IS NOT NULL", name)
	if err != nil {
		log.Println("Error with making database query to find IP and MAC of switch: ", err)
		return "", "", "", err
	}
	defer dbsearch.Close()

	var (
		IP           string
		MAC          string
		UpSwitch     sql.NullString
		upswitchname string
	)

	for dbsearch.Next() {
		err := dbsearch.Scan(&IP, &MAC, &UpSwitch)
		if err != nil {
			log.Println("Error with scanning database for query: ", err)
			return "", "", "", err
		}

		if IP != "" && MAC != "" {
			//Searching upswitch name
			if UpSwitch.Valid {
				upswitchsearch, err := server.Core.DBnetmap.Query("SELECT `name` FROM `host` WHERE ip IS NOT NULL AND `id` = ?", UpSwitch)
				if err != nil {
					log.Println("Error database query for searching upswitch: ", err)
					return "", "", "", err
				}
				defer upswitchsearch.Close()

				for upswitchsearch.Next() {
					err := upswitchsearch.Scan(&upswitchname)
					if err != nil {
						log.Println("Error with searching upswitch name: ", err)
						return "", "", "", err
					}
				}

				err = upswitchsearch.Err()
				if err != nil {
					log.Println("Upswitch searching error: ", err)
					return "", "", "", err
				}
			} else {
				log.Printf("Is no upswitch for %s in database", name)
				return IP, MAC, "", nil
			}
			//End searching upswitch name
		}
	}
	err = dbsearch.Err()
	if err != nil {
		log.Println("Error searching IP and MAC: ", err)
		return "", "", "", err
	}

	return IP, MAC, upswitchname, nil
}

//GetSerial helps to get serial number of switch
func GetSerial(ip, model string) (rev, sernum string, err error) {
	if model == "D-Link" {
		return "", "", errors.New("Can't use snmp on d-link to get serial number")
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
			return "", "", errors.New("Can't get serial number")
		}
	}

	return rev, sernum, nil
}

//AlreadyLogin checks is user already logged in
func AlreadyLogin(r *http.Request) bool {
	session, _ := server.Core.Store.Get(r, "session")
	return session.Values["userName"] != nil
}

//AuthCheck is a middleware for handlers
func AuthCheck(handler http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if !AlreadyLogin(r) {
			http.Redirect(w, r, "/login", http.StatusFound)
			return
		}

		handler(w, r)
	}
}
