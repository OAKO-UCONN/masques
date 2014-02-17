(ns masques.model.profile
  (:require [clj-crypto.core :as clj-crypto]
            [clj-i2p.core :as clj-i2p]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.string-utils :as string-utils]
            [config.db-config :as db-config]
            [masques.model.avatar :as avatar-model])
  (:use masques.model.base
        korma.core)
  (:import [org.apache.commons.codec.binary Base64]
           [java.io PushbackReader]))

(def alias-key :alias)
(def destination-key :destination)
(def identity-key :identity)
(def identity-algorithm-key :identity-algorithm)
(def private-key-key :private-key)
(def private-key-algorithm-key :private-key-algorithm)

(def saved-current-user (atom nil))

; CURRENT USER

(defn current-user
  "Returns the currently logged in user or nil if no user is logged in."
  []
  @saved-current-user)
  
(defn set-current-user
  "Sets the currently logged in user."
  [profile]
  (reset! saved-current-user profile))
  
; SAVE PROFILE

(defn name-avatar [profile-record]
  (str (alias-key profile-record) "'s Avatar"))

(defn insert-avatar [profile-record]
  (let [avatar-file-map { :path (:avatar-path profile-record) :name (name-avatar profile-record) }]
    (avatar-model/create-avatar-image (:avatar-path profile-record))
    (insert-or-update file avatar-file-map)))

(defn save-avatar [profile-record]
  (if (:avatar-path profile-record)
    (merge profile-record { :avatar-file-id (:id (insert-avatar profile-record)) })
    profile-record))

(defn save [record]
  (insert-or-update profile (dissoc (save-avatar record) :avatar :avatar-path)))

(defn save-current-user [record]
  (when-not (find-by-id profile 1)
    (save record)))

; BUILD PROFILE

(defn attach-avatar [profile-record]
  (if (:avatar-file-id profile-record)
    (conj { :avatar (find-by-id file (:avatar-file-id profile-record)) } profile-record)
    profile-record))

(defn build [id]
  (attach-avatar (find-by-id profile id)))

; CREATE USER

(defn generate-keys [profile-record]
  (let [key-pair (clj-crypto/generate-key-pair)
        key-pair-map (clj-crypto/get-key-pair-map key-pair)]
    (merge profile-record { identity-key (Base64/encodeBase64String (:bytes (:public-key key-pair-map)))
                            identity-algorithm-key (:algorithm (:public-key key-pair-map))
                            private-key-key (Base64/encodeBase64String (:bytes (:private-key key-pair-map)))
                            private-key-algorithm-key (:algorithm (:private-key key-pair-map)) })))

(defn create-user [user-name]
  (save (generate-keys { alias-key user-name })))
  
(defn create-friend-profile
  "Creates a profile for a friend where you only have the alias, identity and identity algorithm."
  [alias identity identity-algorithm]
  (save { alias-key alias identity-key identity identity-algorithm-key identity-algorithm }))

(defn find-logged-in-user
  "Finds the profile for the given user name which is a user of this database."
  [user-name]
  (when user-name
    (build (:id (first
      (select profile
        (fields :ID)
        (where { :ALIAS user-name  :PRIVATE_KEY [not= nil]})
        (limit 1)))))))

(defn reload-current-user
  "Reloads the current user from the database. Returns the current user."
  ([] (reload-current-user (db-config/current-username)))
  ([user-name]
    (when-let [user-profile (find-logged-in-user user-name)]
      (set-current-user user-profile)
      user-profile)))

(defn init
  "Loads the currently logged in user's profile into memory. Creating the profile if it does not alreay exist."
  []
  (let [user-name (db-config/current-username)]
    (when-not (reload-current-user user-name)
      (let [new-profile (create-user user-name)]
        (when-not (reload-current-user user-name)
          (throw (RuntimeException. (str "Could not create user: " user-name))))))))

(defn logout
  "Logs out the currently logged in user. Just used for testing."
  []
  (set-current-user nil))

(defn create-masques-id-map
  "Creates a masques id map from the given profile. If the profile is not given, then the current logged in profile is used."
  ([] (create-masques-id-map (current-user)))
  ([profile]
    (assoc (select-keys profile [alias-key identity-key identity-algorithm-key])
           destination-key (clj-i2p/base-64-destination))))
           
(defn create-masques-id-file
  "Given a file and a profile, this function saves the profile as a masques id
to the file. If a profile is not given, then the currently logged in profile is
used."
  ([file] (create-masques-id-file file (current-user)))
  ([file profile]
    (spit file (string-utils/form-str (create-masques-id-map profile)))))

(defn read-masques-id-file 
  "Reads the given masques id file and returns the masques id map."
  [file]
  (edn/read (PushbackReader. (io/reader file))))