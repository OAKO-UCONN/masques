(ns masques.controller.data-directory.choose
  (:require [clojure.tools.logging :as logging]
            [config.db-config :as db-config]
            [masques.controller.actions.utils :as actions-utils]
            [masques.controller.utils :as controller-utils]
            [masques.model.system-properties :as system-properties]
            [masques.view.data-directory.choose :as choose-view]
            [masques.view.utils :as view-utils]
            [seesaw.core :as seesaw-core]))

(def save-listener-key :save-listener-key)

(defn update-data-directory [parent-component path]
  (.setText (choose-view/data-directory-text parent-component) path))

(defn save-save-listener [save-listener parent-component]
  (view-utils/save-component-property
    (choose-view/content-panel parent-component)
    save-listener-key
    save-listener))

(defn retrieve-save-listener [parent-component]
  (view-utils/retrieve-component-property
    (choose-view/content-panel parent-component)
    save-listener-key))

(defn load-data [save-listener parent-component]
  (update-data-directory parent-component (db-config/data-dir))
  (save-save-listener save-listener parent-component)
  parent-component)

(defn create-cancel-action [parent-component]
  (fn [e]
    (actions-utils/close-window parent-component)
    (System/exit 0)))

(defn create-choose-directory-action [parent-component]
  (fn [e]
    (when-let [chosen-directory (controller-utils/choose-directory parent-component)]
      (update-data-directory parent-component (.getPath chosen-directory)))))

(defn create-save-action [parent-component]
  (fn [e]
    (let [data-directory (choose-view/data-directory parent-component)]
      (db-config/update-data-directory data-directory)
      (system-properties/set-data-directory data-directory))
    (actions-utils/close-window parent-component)
    ((retrieve-save-listener parent-component))))

(defn attach [parent-component]
  (choose-view/add-cancel-action parent-component (create-cancel-action parent-component))
  (choose-view/add-choose-action parent-component (create-choose-directory-action parent-component))
  (choose-view/add-save-action parent-component (create-save-action parent-component))
  parent-component)

(defn show [save-listener]
  (if-let [data-directory (system-properties/read-data-directory)]
    (do
      (db-config/update-data-directory data-directory)
      (save-listener))
    (controller-utils/show (attach (load-data save-listener (choose-view/create))))))

(defn click-save [parent-component]
  (choose-view/click-save parent-component))