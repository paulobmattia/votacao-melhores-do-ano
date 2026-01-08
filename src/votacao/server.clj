(ns votacao.server
  "Servidor HTTP para a aplicação de votação Electric Clojure."
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [clojure.java.io :as io])
  (:gen-class))

;; =============================================================================
;; Geração de Session ID
;; =============================================================================

(defn generate-session-id
  "Gera um ID de sessão único para identificar votantes."
  []
  (str (java.util.UUID/randomUUID)))

;; =============================================================================
;; Middleware
;; =============================================================================

(defn wrap-session-id
  "Middleware que garante que cada cliente tenha um session-id único."
  [handler]
  (fn [request]
    (let [session-id (or (get-in request [:cookies "session-id" :value])
                         (generate-session-id))
          response (handler (assoc request :session-id session-id))]
      (if (get-in request [:cookies "session-id" :value])
        response
        (assoc-in response [:cookies "session-id"] 
                  {:value session-id
                   :path "/"
                   :max-age (* 60 60 24)  ; 24 horas
                   :same-site :lax})))))

(defn wrap-electric
  "Middleware placeholder para integração Electric.
   Em produção, isso seria substituído pela integração real do Electric."
  [handler]
  (fn [request]
    (handler request)))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn index-handler
  "Serve o index.html para todas as rotas não-estáticas."
  [request]
  (if-let [resource (io/resource "public/index.html")]
    (-> (response/response (slurp resource))
        (response/content-type "text/html; charset=utf-8"))
    (response/not-found "Página não encontrada")))

(defn routes
  "Roteador principal da aplicação."
  [request]
  (let [uri (:uri request)]
    (cond
      ;; API Health check
      (= uri "/health")
      (-> (response/response "OK")
          (response/content-type "text/plain"))
      
      ;; Arquivos estáticos são servidos pelo wrap-resource
      ;; Todas as outras rotas vão para o index.html (SPA)
      :else
      (index-handler request))))

;; =============================================================================
;; Application Handler
;; =============================================================================

(def app
  (-> routes
      wrap-electric
      wrap-session-id
      (wrap-resource "public")
      wrap-content-type
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

;; =============================================================================
;; Server
;; =============================================================================

(defonce server (atom nil))

(defn start-server!
  "Inicia o servidor na porta especificada."
  [& {:keys [port] :or {port 8080}}]
  (when @server
    (.stop @server))
  (println (str "🚀 Servidor iniciando na porta " port "..."))
  (println (str "📱 Votante:     http://localhost:" port "/?perfil=votante"))
  (println (str "🎛️  Admin:       http://localhost:" port "/?perfil=admin"))
  (println (str "🎬 Apresentador: http://localhost:" port "/?perfil=apresentador"))
  (reset! server (jetty/run-jetty app {:port port :join? false}))
  (println "✅ Servidor pronto!"))

(defn stop-server!
  "Para o servidor."
  []
  (when @server
    (.stop @server)
    (reset! server nil)
    (println "🛑 Servidor parado.")))

(defn -main
  "Ponto de entrada principal."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start-server! :port port)))
