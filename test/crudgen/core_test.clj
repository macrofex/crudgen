(ns crudgen.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [crudgen.core :refer :all]))

(def test-model-file "test/crudgen/models.edn")

(deftest test-loading-models
  (testing "loading the EDN-defined model for side effect testing"
    (is (not (= nil (load-models! test-model-file))))
    )
  )

(deftest test-model-preparation
  (testing "preparing a model should add several keys"
    (let [m (second (first (load-models! test-model-file)))
          p (prepare-model m)
          ]
      (is (not (= nil m)))
      (is (= true (contains? p :model-ns-name)))
      (is (= true (contains? p :model-file-name)))
      (is (= true (contains? p :model-field-names)))
      (is (= true (contains? p :model-key)))
      (is (= true (contains? p :model-nk-names)))
      (is (= true (contains? p :model-nk-fields)))
      (is (= true (contains? p :model-output-path)))
      )
    )
  )

(deftest test-file-scaffolding
  (testing "scaffolding a file for a given model should generate a file output"
    (let [m (second (first (load-models! test-model-file)))
          p (prepare-model m)
          file-to-scaffold "index.template.html"
          output-file "index.html"
          ]
      (do
        (scaffold-file! p file-to-scaffold output-file)
        (is (= true (.exists (io/as-file (str (:model-output-path p) "/" output-file) ))))
        )
      )
    )
  )


(deftest test-model-scaffolding
  (testing "scaffolding a model results in all output files"
    (let [model (second (first (load-models! test-model-file)))
          p (prepare-model model)
          ]
      (do
        (scaffold-model! model scaffold-files-specs (fn [m] (println "No recommendation")))
        (doseq [fs scaffold-files-specs]
          (is (= true (.exists (io/as-file (str (:model-output-path p) "/" ((:output fs) p))))))
          )
        )
      )
    )
  )
