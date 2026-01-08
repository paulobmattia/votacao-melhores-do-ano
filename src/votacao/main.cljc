(ns votacao.main
  "Ponto de entrada principal da aplicação Electric Clojure.
   Roteia para a visão apropriada baseado no parâmetro de query."
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [votacao.state :as state]
            [votacao.ui.votante :as votante]
            [votacao.ui.admin :as admin]
            [votacao.ui.apresentador :as apresentador])
  #?(:cljs (:require-macros [votacao.main])))

;; =============================================================================
;; Utilitários de Roteamento
;; =============================================================================

#?(:cljs
   (defn get-query-param
     "Extrai um parâmetro da query string da URL."
     [param]
     (let [params (js/URLSearchParams. (.-search js/location))]
       (.get params param))))

#?(:cljs
   (defn get-session-id
     "Obtém ou cria um session-id único armazenado em localStorage."
     []
     (let [storage-key "votacao-session-id"
           existing (js/localStorage.getItem storage-key)]
       (if existing
         existing
         (let [new-id (str (random-uuid))]
           (js/localStorage.setItem storage-key new-id)
           new-id)))))

;; =============================================================================
;; Componente de Navegação (Para Debug)
;; =============================================================================

(e/defn NavegacaoDebug
  "Barra de navegação para trocar entre visões (apenas em dev)."
  [perfil-atual]
  (e/client
    (dom/nav
      (dom/props {:class "fixed bottom-0 left-0 right-0 bg-midnight-900/95 backdrop-blur 
                         border-t border-midnight-700 p-4 z-50"})
      (dom/div
        (dom/props {:class "flex justify-center gap-4 max-w-lg mx-auto"})
        
        (e/for [[id label icone] [["votante" "Votante" "📱"]
                                  ["admin" "Admin" "🎛️"]
                                  ["apresentador" "Telão" "🎬"]]]
          (dom/a
            (dom/props {:href (str "?perfil=" id)
                        :class (str "flex-1 text-center py-3 px-4 rounded-xl font-medium "
                                    "transition-all duration-200 "
                                    (if (= perfil-atual id)
                                      "bg-gold-500 text-midnight-950"
                                      "bg-midnight-800 text-midnight-300 hover:bg-midnight-700"))})
            (dom/span (dom/text icone))
            (dom/span
              (dom/props {:class "ml-2 hidden sm:inline"})
              (dom/text label))))))))

;; =============================================================================
;; Componente de Erro
;; =============================================================================

(e/defn TelaErro
  "Tela de erro quando o perfil é inválido."
  []
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-screen text-center px-8"})
      
      (dom/span
        (dom/props {:class "text-6xl mb-6"})
        (dom/text "❌"))
      
      (dom/h1
        (dom/props {:class "text-2xl font-bold text-white mb-4"})
        (dom/text "Perfil Inválido"))
      
      (dom/p
        (dom/props {:class "text-midnight-400 mb-8"})
        (dom/text "Use um dos links abaixo para acessar o sistema:"))
      
      (dom/div
        (dom/props {:class "space-y-4"})
        (e/for [[id label] [["votante" "📱 Tela de Votação"]
                            ["admin" "🎛️ Painel Admin"]
                            ["apresentador" "🎬 Telão/Projetor"]]]
          (dom/a
            (dom/props {:href (str "?perfil=" id)
                        :class "block px-6 py-3 rounded-xl glass-card text-gold-400 
                               hover:border-gold-400 transition-all"})
            (dom/text label)))))))

;; =============================================================================
;; Router Principal
;; =============================================================================

(e/defn Router
  "Componente de roteamento baseado em perfil."
  [perfil session-id]
  (e/client
    (case perfil
      "votante"
      (votante/VisaoVotante. session-id)
      
      "admin"
      (admin/VisaoAdmin.)
      
      "apresentador"
      (apresentador/VisaoApresentador.)
      
      ;; Perfil inválido ou não especificado
      (TelaErro.))))

;; =============================================================================
;; Aplicação Principal
;; =============================================================================

(e/defn App
  "Componente raiz da aplicação Electric."
  []
  (e/client
    (let [perfil #?(:cljs (get-query-param "perfil") :clj "votante")
          session-id #?(:cljs (get-session-id) :clj "server-session")
          show-debug? #?(:cljs (= (get-query-param "debug") "true") :clj false)]
      
      (dom/div
        (dom/props {:class "min-h-screen bg-midnight-950"})
        
        ;; Componente principal baseado no perfil
        (Router. perfil session-id)
        
        ;; Navegação de debug (opcional)
        (when show-debug?
          (NavegacaoDebug. perfil))))))

;; =============================================================================
;; Ponto de Entrada (ClojureScript)
;; =============================================================================

#?(:cljs
   (defn ^:export init!
     "Função de inicialização chamada pelo shadow-cljs."
     []
     (println "🚀 Iniciando aplicação de votação...")
     ;; Electric initialization would go here
     ;; (electric/boot App)
     (println "✅ Aplicação iniciada!")))

#?(:cljs
   (defn ^:dev/after-load reload!
     "Hot reload hook para desenvolvimento."
     []
     (println "🔄 Hot reload...")))
