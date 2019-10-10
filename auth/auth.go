package auth

import (
	"errors"
	"log"
	"net/http"
	"text/template"

	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/go-ldap/ldap"
	"github.com/gorilla/mux"
)

func auth(login, password string) (string, error) {
	if password == "" {
		return "", errors.New("Empty password")
	}

	username := ""

	l, err := ldap.Dial("tcp", server.Conf.LdapServer)
	if err != nil {
		return username, err
	}
	defer l.Close()

	if l.Bind(server.Conf.LdapUser, server.Conf.LdapPassword); err != nil {
		return username, err
	}

	searchRequest := ldap.NewSearchRequest(
		server.Conf.LdapBaseDN,
		ldap.ScopeWholeSubtree, ldap.NeverDerefAliases, 0, 0, false,
		"(&(sAMAccountName="+login+"))",
		[]string{"cn"},
		nil,
	)

	if sr, err := l.Search(searchRequest); err != nil || len(sr.Entries) != 1 {
		return username, errors.New("User not found")
	} else {
		username = sr.Entries[0].GetAttributeValue("cn")
	}

	if err = l.Bind(username, password); err != nil {
		return "", err
	}

	return username, err
}

//Handler handle login page
func Handler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "session")
	if err != nil {
		log.Printf("Error getting session: %s", err)
	}

	vars := mux.Vars(r)
	pagetype := vars["type"]

	switch pagetype {
	case "login":
		if r.Method == "GET" {
			tmpl, err := template.ParseFiles("templates/login.html")
			if err != nil {
				log.Printf("Error parsing template files for login page: %s", err)
			}

			var data helpers.ViewData

			err = tmpl.Execute(w, data)
			if err != nil {
				log.Printf("Error executing template for login page: %s", err)
			}
		} else if r.Method == "POST" {
			if helpers.AlreadyLogin(r) {
				http.Redirect(w, r, "/map", http.StatusFound)
				return
			}

			if userName, err := auth(r.FormValue("uname"), r.FormValue("psw")); err != nil {
				http.Redirect(w, r, "/admin/login", http.StatusFound)
				return
			} else {
				session.Values["userName"] = userName
				session.Values["user"] = r.FormValue("uname")

				err = session.Save(r, w)
				if err != nil {
					log.Printf("Error saving cookies on login: %s", err)
				}

				http.Redirect(w, r, "/map", http.StatusFound)
			}
		}
	case "logout":
		session.Values["userName"] = nil
		session.Values["user"] = nil

		err = session.Save(r, w)
		if err != nil {
			log.Printf("Error saving cookies on logout: %s", err)
		}

		http.Redirect(w, r, "/admin/login", http.StatusFound)
	}
}
