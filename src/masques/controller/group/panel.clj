(ns masques.controller.group.panel
  (:require [clojure.java.io :as java-io]
            [masques.controller.actions.utils :as actions-utils]
            [masques.controller.main.panel-protocol :as panel-protocol]
            [masques.controller.utils :as controller-utils]
            [masques.view.group.panel :as panel-view]
            [seesaw.core :as seesaw-core])
  (:import [masques.controller.main.panel_protocol PanelProtocol]
           [javax.swing ImageIcon]))

(deftype GroupPanel []
  PanelProtocol
  (panel-name [this] "Groups")

  (create-view [this] (panel-view/create))

  (icon [this] (ImageIcon. (ClassLoader/getSystemResource "logo_for_dark_backgrounds_small.png")))

  (init [this view])

  (show [this view & args])

  (hide [this view]))

(defn create []
  (GroupPanel.))