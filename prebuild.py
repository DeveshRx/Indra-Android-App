import subprocess

gs_url_debug=os.environ['url_google_services_json_debug']
gs_url_release=os.environ['url_google_services_json_release']
gs_url_4build=os.environ['url_google_services_json_4build']

subprocess.run("curl -o app/src/debug/google-services.json "+gs_url_debug, shell=True, check=True)
subprocess.run("curl -o app/src/release/google-services.json "+gs_url_release, shell=True, check=True)
subprocess.run("curl -o app/google-services.json "+gs_url_4build, shell=True, check=True)