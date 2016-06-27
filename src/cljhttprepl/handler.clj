(ns cljhttprepl.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class)
)

(def ^:dynamic *token* (atom nil))

(defn run-command [body query]
    (try
        (let [token (get (apply hash-map (str/split query #"=")) "token")]
            (if (and (not (nil? token)) (= token @*token*))
                (str (eval (read-string (slurp body))))
                "Wrong auth token!"))
        (catch Exception e
            (str e)))
)

(defroutes repl-routes
  (POST "/repl" {body :body, query :query-string}
      (if-not (empty? @*token*)
          (run-command body query)
          "No auth token supplied!"))
)

(defn set-repl-token! [ token ]
    (reset! *token* token)
)

(def app
  (wrap-defaults repl-routes site-defaults))

(defn -main [& params]
    (set-repl-token! "456")
    (jetty/run-jetty repl-routes {:port 3000 :join? false})
)    
