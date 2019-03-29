-- :name create-{{model.name|lower}}! :! :n
-- :doc creates a new {{model.name|lower}} record
INSERT INTO {{model.plural}}
({{ model.model-nk-names|join:"," }})
VALUES ({% for f in model.model-nk-names|drop-last:1 %}:{{f}},{% endfor %} :{{model.model-nk-names|last}})

-- :name update-{{model.name|lower}}! :! :n
-- :doc updates an existing {{model.name|lower}} record
UPDATE {{model.plural}}
SET {% for f in model.model-nk-names|drop-last:1 %}{{f}} = :{{f}}, {% endfor %}{{model.model-nk-names|last}} = :{{model.model-nk-names|last}}
WHERE {{key}} = :{{key}}


-- :name get-{{model.name|lower}} :? :1
-- :doc retrieves a {{model.name|lower}} record given the id
SELECT * FROM {{model.plural}}
WHERE id = :id

-- :name delete-{{model.name|lower}}! :! :n
-- :doc deletes a {{model.name|lower}} record given the id
DELETE FROM {{model.plural}}
WHERE id = :id


-- :name get-{{model.plural}} :? :*
-- :doc retrieves all {{model.plural}}
SELECT * FROM {{model.plural}}
