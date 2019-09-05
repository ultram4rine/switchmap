package auth

import (
	"errors"
	"net/http"
	"text/template"

	"github.com/ultram4rine/switchmap/helpers"
	"github.com/ultram4rine/switchmap/server"

	"github.com/gorilla/mux"

	"github.com/go-ldap/ldap"
)

var data helpers.ViewData

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
	session, _ := server.Core.Store.Get(r, "session")
	vars := mux.Vars(r)
	pagetype := vars["type"]

	switch pagetype {
	case "login":
		if r.Method == "GET" {
			tmpl, _ := template.ParseFiles("templates/login.html")
			tmpl.Execute(w, data)
		} else if r.Method == "POST" {
			r.ParseForm()

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
				session.Save(r, w)
				http.Redirect(w, r, "/map", http.StatusFound)
			}
		}
	case "logout":
		session.Values["userName"] = nil
		session.Values["user"] = nil
		session.Save(r, w)
		http.Redirect(w, r, "/admin/login", 301)
	}
}
