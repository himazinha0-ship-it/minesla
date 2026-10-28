"""Run Gradle with the managed environment's proxy, without exposing credentials."""
import os
from pathlib import Path
import subprocess
import sys
from urllib.parse import urlsplit

root = Path(__file__).resolve().parents[1]
build_env = os.environ.copy()
prepared_jdk = Path('/workspace/tools/jdk-21.0.9+10')
current_jdk = Path(build_env.get('JAVA_HOME', '/nonexistent'))
if not (current_jdk / 'bin/javac').is_file() and (prepared_jdk / 'bin/javac').is_file():
    build_env['JAVA_HOME'] = str(prepared_jdk)

gradle = Path('/workspace/tools/gradle-8.8/bin/gradle')
command = [str(gradle)] if gradle.is_file() else [str(root / 'gradlew')]
command += ['--no-daemon', '--console=plain']
for scheme in ['http', 'https']:
    proxy_url = build_env.get(scheme.upper() + '_PROXY') or build_env.get(scheme + '_proxy')
    if proxy_url:
        proxy = urlsplit(proxy_url)
        if proxy.username or proxy.password:
            raise SystemExit('Proxy autenticado: configure o Gradle por uma integração segura; não exponha credenciais na linha de comando.')
        if proxy.hostname:
            command += [f'-D{scheme}.proxyHost={proxy.hostname}', f'-D{scheme}.proxyPort={proxy.port or 80}']
command += sys.argv[1:] or ['build']
raise SystemExit(subprocess.call(command, cwd=root, env=build_env))
