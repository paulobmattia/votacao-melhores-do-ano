(ns dev
  "Namespace de desenvolvimento."
  (:require [votacao.server :as server]))

(defn go
  "Inicia o servidor de desenvolvimento."
  []
  (server/start-server! :port 8080))

(defn halt
  "Para o servidor."
  []
  (server/stop-server!))

(defn reset
  "Reinicia o servidor."
  []
  (halt)
  (go))

(comment
  ;; REPL workflow
  (go)    ; Inicia servidor
  (halt)  ; Para servidor
  (reset) ; Reinicia servidor
  
  ;; Acessar as URLs:
  ;; http://localhost:8080/?perfil=votante
  ;; http://localhost:8080/?perfil=admin
  ;; http://localhost:8080/?perfil=apresentador
  
  ;; Manipulação direta do estado (para testes)
  (require '[votacao.state :as state])
  
  @state/!game-state
  
  (state/abrir-votacao!)
  (state/votar! "test-user-1" 101)
  (state/votar! "test-user-2" 102)
  (state/fechar-votacao!)
  (state/revelar-vencedor!)
  (state/proxima-categoria!)
  
  (state/resetar-categoria!))
