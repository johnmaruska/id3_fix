(ns id3-fix.core
  (:require [claudio.id3 :as id3]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:gen-class))

(def HOME "c:\\Users\\jackm")
(def ROOT-DIR (io/file HOME "Music" "Ripped CDs"))

;; disable noisy verbose logger
(.setLevel (java.util.logging.Logger/getLogger "org.jaudiotagger")
           java.util.logging.Level/OFF)

;; Clojure wrappers on java.io.File
(defn get-name   [file] (.getName file))
(defn directory? [file] (.isDirectory file))
(defn list-files [file] (.listFiles file))

(defn get-contained-directories [dir]
  (filter #(.isDirectory %) (list-files dir)))

(defn get-disc-dirs
  "Get all directories associated with a disc assuming they're nested below an
  artist directory from the root."
  [root-dir]
  (->> (get-contained-directories ROOT-DIR)  ; artist directories
       (map get-contained-directories)       ; disc directories
       flatten))

(defn get-mp3s [dir]
  (filter #(s/ends-with? (get-name %) ".mp3") (list-files dir)))

(defn all-present? [{:keys [album artist genre title track-total] :as tags}]
  (not (or (s/starts-with? album "Unknown album")
           (= artist "Unknown artist")
           (= genre "Unknown genre")
           (re-matches #"Track \d+$" title))))

(defn get-correct-tags [files]
  (-> (first (filter all-present? (map id3/read-tag files)))
      (assoc :track-total (str (count files)))
      (dissoc :track :title)))

(defn get-broken-tracks [files]
  (filter (comp not all-present? id3/read-tag) files))

(defn remove-mp3-extension [s]
  (first (s/split s #".mp3")))

(defn get-track-title [file]
  (->> (s/split  (get-name file) #" ")
       (drop 1)
       (s/join " ")
       remove-mp3-extension))

(defn write-tags! [tags file]
  (let [tags (assoc tags :title (get-track-title file))]
    (println "Writing file" (get-name file) "with tags" tags)
    (apply id3/write-tag! file (flatten (vec tags)))))

(defn correct-tracks! [dir]
  (let [mp3s          (get-mp3s dir)
        tags          (get-correct-tags mp3s)
        broken-tracks (get-broken-tracks mp3s)]
    (run! (partial write-tags! tags) broken-tracks)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run! correct-tracks! (get-disc-dirs ROOT-DIR)))
