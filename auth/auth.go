package auth

import (
	"errors"
	"log"
	"net/http"

	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/go-ldap/ldap"
	"github.com/gorilla/mux"
)

func auth(login, password string) error {
	if password == "" {
		return errors.New("empty password")
	}

	l, err := ldap.Dial("tcp", server.Conf.LdapServer)
	if err != nil {
		return err
	}
	defer l.Close()

	if err = l.Bind(server.Conf.LdapUser, server.Conf.LdapPassword); err != nil {
		return err
	}

	searchRequest := ldap.NewSearchRequest(
		server.Conf.LdapBaseDN,
		ldap.ScopeWholeSubtree, ldap.NeverDerefAliases, 0, 0, false,
		"(&(sAMAccountName="+login+"))",
		[]string{"cn"},
		nil,
	)

	sr, err := l.Search(searchRequest)
	if err != nil || len(sr.Entries) != 1 {
		return errors.New("user not found")
	}

	username := sr.Entries[0].GetAttributeValue("cn")

	if err = l.Bind(username, password); err != nil {
		return err
	}

	return nil
}

//Handler handle login page
func Handler(w http.ResponseWriter, r *http.Request) {
	session, err := server.Core.Store.Get(r, "switchmap_session")
	if err != nil {
		log.Printf("Error getting session for login page: %s", err)
		return
	}

	vars := mux.Vars(r)
	pagetype := vars["type"]

	switch pagetype {
	case "login":
		if r.Method == "GET" {
			http.ServeFile(w, r, "public/html/login.html")
		} else if r.Method == "POST" {
			if helpers.AlreadyLogin(r) {
				http.Redirect(w, r, "/map", http.StatusFound)
				return
			}

			if err := auth(r.FormValue("uname"), r.FormValue("psw")); err != nil {
				http.Redirect(w, r, "/admin/login", http.StatusFound)
				return
			}

			session.Values["user"] = r.FormValue("uname")

			err = session.Save(r, w)
			if err != nil {
				log.Printf("Error saving cookies on login: %s", err)
			}

			http.Redirect(w, r, "/map", http.StatusFound)
		}
	case "logout":
		session.Values["user"] = nil

		err = session.Save(r, w)
		if err != nil {
			log.Printf("Error saving cookies on logout: %s", err)
		}

		http.Redirect(w, r, "/admin/login", http.StatusFound)
	}
}
