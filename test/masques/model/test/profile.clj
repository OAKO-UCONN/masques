(ns masques.model.test.profile
  (:require test.init)
  (:use clojure.test
        masques.model.profile))

(def profile-map {
  :alias "Ted"
  :avatar-path "/Users/Ted/masques/avatar.png"
})

(deftest test-add-profile
  (let [profile-record (save profile-map)]
    (is profile-record)
    (is (:id profile-record))
    (is (= (:alias profile-record) "Ted"))
    (is (instance? org.joda.time.DateTime (:created-at profile-record)))))

(deftest test-build-profile
  (let [built-profile (build (:id (save profile-map)))]
    (println "\n\nBUILT PROFILE\n\n" built-profile)
    (is built-profile)
    (is (map? (:avatar built-profile)))
    (is (= (:id (:avatar built-profile)) (:avatar-file-id built-profile)))
    (is (= (:path (:avatar built-profile)) (:avatar-path profile-map)))))

(deftest test-create-user-profile
  (let [user-profile (create-user "Ted")]
    (is user-profile)
    (is (= (:alias user-profile) "Ted"))))

