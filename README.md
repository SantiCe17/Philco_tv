# Philco TV IPTV (Android TV)

Projeto inicial de IPTV para Android TV 11. Interface simples com navegação por controle remoto e reprodução via Media3/ExoPlayer. A playlist padrão é a lista completa do projeto IPTV-org.

## Gerar APK sem Android Studio (GitHub Actions)

1. Crie um repositório GitHub vazio chamado `philco-tv`.
2. Extraia este ZIP no computador.
3. No repositório, escolha **Add file → Upload files** e envie os arquivos e pastas extraídos (incluindo `.github`).
4. Faça commit na branch `main`.
5. Abra a aba **Actions**, selecione **Build Philco TV APK** e execute **Run workflow** se o build não começou automaticamente.
6. Quando terminar, abra a execução e baixe o artefato `philco-tv-apk`.
7. Dentro do ZIP baixado estará `app-debug.apk`.

## Instalar na TV

- Ative Opções do desenvolvedor (Configurações → Preferências do dispositivo/Sistema → Sobre → pressione Build várias vezes).
- Em Segurança/Apps, permita instalar apps desconhecidos para o método que usar.
- Transfira o APK para a TV (USB ou aplicativo de envio de arquivos) e instale.
- Abra **Philco TV** pela lista de apps.

## Observações

- A lista completa pode conter milhares de canais; o carregamento inicial pode demorar e a disponibilidade dos streams varia.
- Este app é um protótipo inicial: não inclui logos, favoritos persistentes, categorias ou substituição do launcher do sistema.
- Use somente streams que você tenha direito de acessar.
