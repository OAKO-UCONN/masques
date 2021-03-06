(ns masques.service.actions.request-friendship
  (:require [clojure.tools.logging :as logging]
            [masques.model.friend-request :as friend-request-model]
            [masques.model.profile :as profile-model]
            [masques.model.share :as share]
            [masques.service.request-map-utils :as request-map-utils]))

(def action "request-friendship")

(def received-key :received?)

(defn data
  "Returns the data map from the request-map."
  [request-map]
  (:data request-map))

(defn read-friend-profile
  "Returns the friend profile which comes from the person requesting frienship."
  [request-map]
  (let [from-destination (request-map-utils/from-destination request-map)]
    (when-let [profile (:profile (data request-map))]
      (assoc profile profile-model/destination-key from-destination))))

(defn read-message
  "Reads the friend request message from the given request map."
  [request-map]
  (:message (data request-map)))

(defn request-friendship
  "Creates a friend-request from the given request-map and saves it to the
database."
  [request-map]
  (let [friend-profile (read-friend-profile request-map)
        message (read-message request-map)]
    (friend-request-model/receive-request friend-profile message))
  true)

(defn run
  "Creates a new friend request. If successful, returns true. Otherwise, throws
an exception."
  [request-map]
  { :data { :received? (request-friendship request-map) }})