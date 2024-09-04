(ns boardly.core
  (:require [reagent.core :refer [atom]]
            [reagent.dom :as d]
            ["react-select/creatable" :default Select]
            [boardly.bgs :refer [bgs]]))

(def guesses (atom []))
(def endgame-state (atom nil))
(def chosen-game (rand-nth bgs))
(def max-guesses 5)

(defn bg-option-format [bg]
  {:value (:id bg) :label (str (:name bg) " (" (:yearpublished bg) ")")})

(def select-bg-options
  (->> bgs
       (map bg-option-format)
       (sort-by :label)))

(defn check-guess [guess-id]
  (let [guess (first (filter (fn [bg] (= (:id bg) guess-id)) bgs))]
    (swap! guesses conj guess)
    (if (= guess chosen-game)
      (reset! endgame-state :won)
      (when (>= (count @guesses) max-guesses)
        (reset! endgame-state :lost)))))

(defn render-attribute [attribute value baseline]
  (if (= "Rank" attribute)
    (cond
      (> (int value) (int baseline)) "⬆️"
      (< (int value) (int baseline)) "⬇️"
      :else "✅")
    (cond
      (< (int value) (int baseline)) "⬆️"
      (> (int value) (int baseline)) "⬇️"
      :else "✅")))

(defn attribute-card [attribute value baseline]
  [:div {:key attribute :class "guess-attr"}
   [:div attribute]
   [:div
    [:div (str value " " (render-attribute attribute value baseline))]]])

(def guess-attrs
  {:rank          "Rank"
   :minplayers    "Min Players"
   :maxplayers    "Max Players"
   :playingtime   "Play Time"
   :yearpublished "Year"})

(defn render-guess [g]
  [:div {:key (str "guess-" (random-uuid)) :class "guess" :style {:background-image (str "url('" (:image g) "')")}}
   [:div {:class "guess-title"} (:name g)]
   [:div {:class "attr-container"}
    (map (fn [a] (attribute-card (a guess-attrs) (a g) (a chosen-game))) (keys guess-attrs))]])

(defn credits []
  [:p {:class "credits"} "made with ❤️ by "
   [:a {:href "https://github.com/strobelt"} "strobelt"]])

(defn render-endgame [state]
  [:div {:key (:id chosen-game) :class "endgame"}
   [:div {:class "game-container"}
    [:h4 (case state
           :won (str "Congratz! 🎉 You made it in " (count @guesses) " guesses")
           "Better luck next time...")]
    [:div {:class "game-title"} "The game was "
     [:b (:name chosen-game)]]
    [:img {:src (:image chosen-game) :class "game-image"}]
    [:div {:class "attr-container"}
     (map (fn [a] (attribute-card (a guess-attrs) (a chosen-game) (a chosen-game))) (keys guess-attrs))]
    (credits)]])

;; -------------------------
;; Views

(defn home-page []
  [:div

      [:div
    (when (not (nil? @endgame-state))
      [:div {:class "overlay"}
       (render-endgame @endgame-state)])]


   [:span.main
    [:h1 "Boardly"]

    [:div {:class "guess-amount"} "Guesses (" (count @guesses) "/" max-guesses ")"]
    [:> Select {:options select-bg-options
                :isSearchable true
                :autoFocus true
                :captureMenuScroll true
                :closeMenuOnSelect true
                :isValidNewOption (fn [] nil)
                :on-change (fn [e] (check-guess (-> e .-value)))}]

    [:div
     (map render-guess (reverse @guesses))]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [:div
             [home-page]
             [:footer
              (credits)]] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))

