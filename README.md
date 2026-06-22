# 🗺️ RoraiTour - Seu Guia Turístico Digital

O **RoraiTour** é um aplicativo Android robusto e moderno projetado para transformar a experiência de turistas no estado de Roraima e em todo o mundo. Através de um sistema inteligente de geolocalização e integração com múltiplas fontes de dados, o app permite descobrir pontos turísticos, hotéis, restaurantes e locais históricos com descrições ricas e navegação precisa.

---

## 🎨 Design e Experiência do Usuário (UX/UI)
O aplicativo foi construído seguindo as diretrizes do **Material Design 3**, com foco em elegância e legibilidade:
- **Paleta Pastel Blue**: Tons suaves de azul para reduzir a fadiga visual.
- **Tema Dinâmico (Day/Night)**: Suporte nativo ao Modo Escuro, ajustando cores e contrastes automaticamente.
- **Interface Imersiva**: O mapa ocupa a tela cheia, com elementos de busca e filtros flutuantes.
- **Painéis Deslizantes (Bottom Sheets)**: Listas de locais que surgem suavemente sem ocultar totalmente o mapa de contexto.

---

## 🚀 Funcionalidades Principais

### 1. Autenticação e Perfil Local
- **Segurança Offline**: Sistema de login e cadastro 100% local utilizando banco de dados SQLite com criptografia de senha (SHA-256).
- **Gestão de Perfil**: O usuário pode alterar seu nome, atualizar sua senha (com validação da senha atual) e personalizar sua foto de perfil.
- **Sincronização Visual**: A foto e o nome do usuário são atualizados em tempo real no menu lateral (Navigation Drawer).

### 2. Exploração de Mapas e Geolocalização
- **Motor de Mapas**: Utiliza a biblioteca **OSMDroid** (OpenStreetMap), oferecendo mapas detalhados sem dependência obrigatória do Google Maps API.
- **Geolocalização em Tempo Real**: Integração com **Google Play Services (FusedLocationProvider)** para obter a localização exata do usuário com baixo consumo de bateria.
- **Filtros por Categoria**: Explore locais próximos filtrando por "Natureza", "Cultura", "Histórico", "Religião" ou veja seus próprios lugares personalizados.

### 3. Conteúdo Enriquecido via APIs
- **OpenTripMap API**: Fornece as coordenadas, categorias e nomes de milhões de locais de interesse ao redor do mundo.
- **Wikipedia REST API**: O app extrai automaticamente resumos históricos e imagens de alta qualidade da Wikipedia para cada local selecionado.
- **Navegação Inteligente**: Botão de rota que integra com o Google Maps instalado no dispositivo para traçar o caminho até o destino.

### 4. Personalização e Favoritos
- **Meus Lugares (CRUD)**: Permite ao usuário cadastrar seus próprios pontos turísticos, incluindo nome, descrição personalizada, latitude, longitude e imagem.
- **Lista de Favoritos**: Salve locais encontrados no mapa para acesso rápido. Os favoritos aparecem tanto na aba dedicada quanto diretamente no perfil do usuário.

---

## 🏗️ Arquitetura Técnica

O projeto adota o **Repository Pattern**, garantindo que a lógica de interface (Activities) seja separada da lógica de dados.

### Componentes Principais:
- **Activities**: `MainActivity` (Mapa/Busca), `ProfileActivity` (Gestão de conta e favoritos), `DetailActivity` (Dados das APIs).
- **Repositories**: `AuthLocalRepository` (SQLite), `OpenTripMapRepository` (Retrofit), `WikipediaRepository` (Retrofit), `FavoriteRepository`.
- **Database**: SQLite gerenciado pela classe `DatabaseHelper`, versão 3.

---

## 🛠️ Stack de Tecnologias

- **Linguagem**: Java 11 (Android SDK).
- **Rede**: `Retrofit 2` & `Gson` para consumo de APIs JSON.
- **Imagens**: `Glide 4` para carregamento assíncrono, processamento circular e cache de imagens.
- **Mapas**: `OSMDroid` 6.1.20.
- **Localização**: `play-services-location`.
- **Persistência**: `SQLite` & `SharedPreferences`.

---

## ⚙️ Como Configurar o Projeto

1. **Chaves de API**:
   - Obtenha uma chave gratuita em [OpenTripMap](https://opentripmap.io/).
   - Insira sua chave no arquivo `src/main/java/com/example/roraitour/utils/AppConfig.java`.

2. **Compilação**:
   - Abra o projeto no Android Studio.
   - Sincronize o Gradle (`Sync Project with Gradle Files`).
   - Execute o app em um emulador ou dispositivo físico com GPS ativado.

---

## ✒️ Autores
Desenvolvido como projeto acadêmico de Desenvolvimento Mobile por:
- **Kaio Guilherme**
- **Wandressa Reis**

---
*Este aplicativo foi desenvolvido de forma independente, focando em performance, privacidade (dados locais) e riqueza de informações turísticas.*
