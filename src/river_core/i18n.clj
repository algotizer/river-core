(ns river-core.i18n
  {:boot/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util :as util]
    [io.perun.core :as perun]
    [river-core.util :as ru]))

(defn- default-lang-fn [m]
  "Parses `lang` portion out of the filename in the format: lang-title.ext"
  (let [ls (clojure.string/split (:filename m) #"[-\.]")
        langs (first ls)
        lang-parts (clojure.string/split langs #"[_]")
        norm-langs (if (> (count lang-parts) 1)
                     [(first lang-parts) (clojure.string/upper-case (second lang-parts))]
                     lang-parts)
        lang (clojure.string/join "-" norm-langs)]
      (keyword lang)))

(deftask lang
  "Adds :lang key to files metadata. Lang is derived from files metadata."
  [s lang-fn LANGFN code "function to build Lang from files metadata"]
  (boot/with-pre-wrap fileset
    (let [lang-fn       (or lang-fn default-lang-fn)
          files         (perun/get-meta fileset)
          updated-files (map #(assoc % :lang (lang-fn %)) files)]
      (perun/report-debug "lang" "generated slugs" (map :lang updated-files))
      (perun/report-info "lang" "added langs to %s files" (count updated-files))
      (perun/set-meta fileset updated-files))))

(defn default-lang-page
  "a hiccup template for generating a redirect-page for default language"
  [{global :meta files :entries :as all}]
  (str
    "<!DOCTYPE html>"
    "<html>"
      "<head>"
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
        "<meta http-equiv=\"Refresh\" content=\"0; URL=/" (name (get-in global [:site-languages 0] :en)) "/\">"
        "<title>Redirect</title>"
      "</head>"
      "<body>"
        "<a href=\"/" (name (get-in global [:site-languages 0] :en)) "/\">" (name (get-in global [:site-languages 0] :en)) "</a>"
      "</body>"
    "</html>"))
