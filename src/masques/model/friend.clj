(ns masques.model.friend
  (:require [clj-i2p.core :as clj-i2p]
            [clj-record.boot :as clj-record-boot]
            [clojure.data.xml :as data-xml]
            [clojure.java.io :as io]
            [masques.model.identity :as identity]
            [masques.model.user :as user])
  (:use masques.model.base)
  (:import [java.io FileOutputStream OutputStreamWriter]))

(clj-record.core/init-model
  (:associations (belongs-to identity)
                 (belongs-to friend :fk friend_id :model identity)))

(defn all-friends
  "Returns all of the friends for the current identity"
  ([] (all-friends (identity/current-user-identity)))
  ([identity]
    (find-records { :identity_id (:id identity) })))

(defn add-friend
  "Removes the given friend for the given or current identity."
  ([friend-identity] (add-friend friend-identity (identity/current-user-identity)))
  ([friend-identity identity]
    (when-let [identity-id (:id identity)]
      (when-let [friend-id (:id friend-identity)]
        (insert { :identity_id identity-id :friend_id friend-id })))))

(defn friend?
  "Returns true if the given friend is a friend of the given or current identity."
  ([friend-identity] (friend? friend-identity (identity/current-user-identity)))
  ([friend-identity identity]
    (when-let [identity-id (:id identity)]
      (when-let [friend-id (:id friend-identity)]
        (find-record { :identity_id identity-id :friend_id friend-id })))))

(defn remove-friend
  "Removes the given friend for the given or current identity."
  ([friend-identity] (remove-friend friend-identity (identity/current-user-identity)))
  ([friend-identity identity]
    (when-let [friend-to-remove (friend? friend-identity identity)]
      (destroy-record friend-to-remove))))

(defn friend-xml
  "Returns the xml needed to add the logged in user as a friend to another peer."
  ([] (friend-xml (user/current-user) (clj-i2p/base-64-destination)))
  ([user destination]
    (when destination
      (when-let [user-xml (user/xml user)]
        (data-xml/element :friend {}
          user-xml
          (data-xml/element :destination {} (clj-i2p/as-destination-str destination)))))))

(defn write-friend-xml
  "Writes the friend xml to the given file. File can be either a java File class or a string."
  ([file] (write-friend-xml file (user/current-user) (clj-i2p/base-64-destination)))
  ([file user destination]
    (when-let [output-xml (friend-xml user destination)]
      (when-let [java-file (io/as-file file)]
        (with-open [output-stream (FileOutputStream. java-file)]
          (with-open [output (OutputStreamWriter. output-stream "UTF-8")]
            (data-xml/emit output-xml output)))))))