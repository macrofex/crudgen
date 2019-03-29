(ns crudgen.core
  (:use [clojure.pprint :as pp]
        [clojure.string :as s])
  (:require [cprop.core :refer [load-config]]
            [clojure.java.io :refer [make-parents]]
            [selmer.parser :refer [render-file add-tag!]])
  (:gen-class))


(defn ppxml [xml]
  (let [in (javax.xml.transform.stream.StreamSource.
            (java.io.StringReader. xml))
        writer (java.io.StringWriter.)
        out (javax.xml.transform.stream.StreamResult. writer)
        transformer (.newTransformer
                     (javax.xml.transform.TransformerFactory/newInstance))]
    (.setOutputProperty transformer
                        javax.xml.transform.OutputKeys/INDENT "yes")
    (.setOutputProperty transformer
                        "{http://xml.apache.org/xslt}indent-amount" "2")
    (.setOutputProperty transformer
                        javax.xml.transform.OutputKeys/METHOD "xml")
    (.transform transformer in out)
        (-> out .getWriter .toString)))


(add-tag! :start-encoded-braces
  (fn [args context-map]
    (str "{{")))

(add-tag! :end-encoded-braces
  (fn [args context-map]
    (str "}}")))

(add-tag! :start-tag-brace
  (fn [args context-map]
    (str "{%")))

(add-tag! :end-tag-brace
  (fn [args context-map]
    (str "%}")))


(def scaffold-files-specs
  [{:input "route.template.txt"  :output (fn [m] (str (:model-file-name m) ".clj")) }
   {:input "index.template.html" :output (fn [m] (str "index.html") )  }
   {:input "new.template.html"   :output (fn [m] (str "new.html") ) }
   {:input "edit.template.html"  :output (fn [m] (str "edit.html") ) }
   {:input "db.queries.sql"      :output (fn [m] (str (:model-file-name m) ".sql"))}
   ])


(defn suggest-manual-steps!
  "Print code snippets that must be manually inserted or created."
  [model]
  (let [ m-name (s/lower-case (:name model))
         m-plural-name (:plural model)
        ]
        (println "Add the following content to the identified files.")
        (println "")
        (println "src/clj/amw/handler.clj")
        (println "----------------------------------------------------------------")
        (println (str "[amw.routes." m-name "  :refer [" m-name "-routes]]" ) )
        (println "")
        (println (format "(-> #'%s-routes" m-name) )
        (println "   (wrap-routes middleware/wrap-csrf)")
        (println "   (wrap-routes middleware/wrap-formats))")
        (println "")
        (println "src/clj/amw/db/core.clj")
        (println "----------------------------------------------------------------")
        (println (format "(conman/bind-connection *db* 'sql/queries.sql' 'sql/%s.sql' )" m-name ) )
        (println "")
        (println "Create a migration file")
        (println "----------------------------------------------------------------")
        (println "lein repl")
        (println "(start)")
        (println (format "(user/create-migration 'add-%s-table')" m-name) )
        (println "(exit)")
        (println "")
        (println "Then, create the content and run the migration.")
        (println "")
        (println "resources/migrations")
        (println "----------------------------------------------------------------")
        (println (format "CREATE TABLE %s" m-plural-name) )
        (println " (id SERIAL PRIMARY KEY,")
        (println " name VARCHAR(50) NOT NULL,")
        (println " description VARCHAR(50) )")
        (println "")
        (println "Now, run the migration:")
        (println "lein run migrate")
    )
  )


(defn prepare-model
  "Adds convinient elements to the model map, such as list of non-key fields,
   all field names, clojure friendly file name and namespace version of the model name,
   as follows:

   k(:model-file-name) v(converts :name to lowercase and - to _)
   k(:model-ns-name) v(converts :name to lowecase and leaves - as as -)
   k(:model-field-names) v(creates a collection of just field names)
   k(:model-key) v(identifies the name of THE key field)
   k(:model-nk-names) v(name of all non-key fields)
   k(:model-nk-fields) v(collection of all non-key fields)
  "
  [model]
  (let [lcn (s/lower-case (:name model))
        lcnp (s/lower-case (:plural model))
        model-ns-name (s/replace lcn  #"_" "-")
        model-ns-name-plural (s/replace lcnp #"_" "-")
        model-file-name (s/replace lcn #"-" "_")
        model-db-plural-name (s/replace lcnp #"-" "_")
        model-field-names (map #(:name %) (:fields model))
        model-key (first (remove nil? (map #(if (:key %) (:name %)) (:fields model))))
        model-nk-names (remove nil? (map #(if-not (:key %) (:name %)) (:fields model)))
        model-nk-fields (remove nil? (map #(if-not (:key %) %) (:fields model)))
        model-output-path (str "output/" model-file-name)
        ]
    (-> model
        (assoc :model-ns-name model-ns-name)
        (assoc :model-file-name model-file-name)
        (assoc :model-field-names model-field-names)
        (assoc :model-key model-key)
        (assoc :model-nk-names model-nk-names)
        (assoc :model-nk-fields model-nk-fields)
        (assoc :model-output-path model-output-path)
        )))

(defn scaffold-file!
  "Generate an output file given a model and the input template file"
  [model template-name output-name]
  (do
    (println (str "---> rendering: " template-name " to: " output-name))
    (make-parents (str (:model-output-path model) "/.txt"))
    (spit (str (:model-output-path model) "/" output-name )
          (render-file template-name {:model model}) )
    )
  )


(defn scaffold-model!
  "Take a model specification and file (template) specifications and generate
  an output for each file using the attributes in the model."
  [original-model template-specs recommend-fn]
  (let
      [model (prepare-model original-model)]
    (do
      (make-parents (str (:model-output-path model) "/.txt"))

      (doseq [fs template-specs]
        (println (str "---> outputing to " ((:output fs) model)))
        (scaffold-file! model (:input fs) ((:output fs) model))
        )
      (recommend-fn model)
      )
    )
  )

(defn scaffold-models!
  "Scaffolds each model with a :generate = true key"
  [models template-specs recommend-fn]
  (doseq [m models]
    (if (:generate (second m))
       (scaffold-model! (second m) template-specs recommend-fn)
      )))


(defn load-models!
  "Loads the EDN-defined models"
  [models-path]
  (read-string (slurp models-path))
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (scaffold-models! (load-models!  "resources/models.edn") scaffold-files-specs suggest-manual-steps!)
  )
