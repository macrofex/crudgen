# crudgen

A simple tool to generate a CRUD module for a Luminus-based server-side rendered web application. Given the definition of a modele in your domain, this tool will generate a Luminus-based (Compojure) route file, template for Index, New, and Edit view, and a supporting HugSQL file.

A model definition is based on a simple EDN structure as illustrated below:

```EDN

{
 :person {
   :name "Person"
   :plural "people"
   :generate true
   :fields [
      {:name "id" :key true :label "id" :type "integer" :required true}
      {:name "first_name" :key false  :label "First Name" :type "text" :required true}
      {:name "middle_name" :key false  :label "Middle Name" :type "text" :required false}
      {:name "last_name" :key false  :label "Last Name" :type "text" :required true}
      {:name "email"  :key false :label "Email" :type "enail" :required false}
      {:name "home_phone"  :key false :label "Home Phone" :type "text" :required false}
      {:name "mobile_phone" :key false  :label "Mobile Phone" :type "text" :required false}
      {:name "street1" :key false  :label "Street 1" :type "text" :required false}
      {:name "street2" :key false  :label "Street 2" :type "text" :required false}
      {:name "city" :key false  :label "City" :type "text" :required false}
      {:name "state" :key false  :label "State" :type "text" :required false}
      {:name "zip_code" :key false  :label "Zip Code" :type "text" :required false}
   ]
  }
 :appointment {
   :name "Appointment"
   :plural "appointments"
   :generate true
   :fields [
     ...
   ]
 }
}

``` 

The tool is driven by reading `/resources/models.edn` which includes the definition of one or more models, each one identified by a keyword (e.g., :person). Each model results in the generation of the following files:

```
 {model-name}.clj
 {model-name}.sql
 index.html
 new.html
 edit.html
```

For example, given the above definition of the :person model, the following `person.clj` file would be generated:

```clojure
(ns amw.routes.person
(:require [amw.layout :as layout]
            [amw.db.core :as db]
            [compojure.core :refer [defroutes GET POST PUT]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :as response]
            [struct.core :as st]
            [ring.middleware.anti-forgery :as af]
            [ring.middleware.anti-forgery.strategy :as strategy]
            [compojure.coercions :refer [as-int]]
            [clojure.java.io :as io])
  )

;;
;; http://funcool.github.io/struct/latest/#quick-start
;;
(def person-schema
  {
   
     :id [st/string]  
     :first_name [[st/required :message "You must provide First Name."] [st/string]]   
     :middle_name [st/string]  
     :last_name [[st/required :message "You must provide Last Name."] [st/string]]   
     :email [st/string]  
     :home_phone [st/string]  
     :mobile_phone [st/string]  
     :street1 [st/string]  
     :street2 [st/string]  
     :city [st/string]  
     :state [st/string]  
     :zip_code [st/string] 
   ;; examples of validations
   ;; :home_phone [[st/min-count 10 :message "Invalid Home phone number. Must have 10 digits or more."]]
   ;; :zipcode [[st/max-count 9 :message "Invalid Zip Code. Cannot have more than 9 digits"]]
   })

(defn index-action [request]
  (let [ (db/get-)]
    (layout/render request "person/index.html" { :collection   })
    )
  )

(defn new-action [request]
  (layout/render request "person/new.html" {:username "Some value"}))


(defn create-action [request]
  (do
    (db/create-person! (:params request))
    (redirect (str "/person"))
   )
  )

(defn edit-action [id]
  (let [person (db/get-person {:id id})]
    (layout/render {:params {:id id}} "person/edit.html" {:person person} )
    )
  )

(defn update-action [raw-request]
  (let [request (:params raw-request)
        validated-request (st/validate request person-schema)
        validation-result (first validated-request)
        person (second validated-request)]
    (if (nil? validation-result)
      (do
        (db/update-person! person)
        (redirect (str "/person"))
        )
      (do
        (layout/render {:params {:id (:id person  ) } } "person/edit.html" {:person person :errors validation-result}  )
       )
      )
    )
  )

(defroutes person-routes
  (GET   "/person"           request (index-action request))
  (GET   "/person/new"       request (new-action request))
  (GET   "/person/:id/edit"  [id :<< as-int] (edit-action id))
  (POST  "/person/update"    request (update-action request))
  (POST  "/person/create"    request (create-action request))
  )
```

These files are generated in the `/output/{model-name}` folder.


## Installation

Clone this repo:

```
git clone git@github.com:macrofex/crudgen.git
```

## Usage




## Examples

## License

Copyright © 2019 MACFOFEX LLC

Distributed under the Eclipse Public License either version 1.0 or any later version.
