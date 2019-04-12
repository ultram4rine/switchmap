package helpers

import (
	"errors"
	"net/http"
	"repos/switchmap/server"

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
	Swits  []Switch    //Swits to show information of switch on plan
	Builds []Build     //Builds to show build on map
	Floors []Floor     //Floors to show floors in build
}

//GetSerial helps to get serial number of switch
func GetSerial(ip, model string) (rev, sernum string, err error) {
	if model != "HP-ProCurve" {
		return "", "", errors.New("Can't use snmp to cisco or d-link to get serial number")
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
