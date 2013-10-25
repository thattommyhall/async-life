set :application, "async-life"
set :repository,  "git@github.com:thattommyhall/async-life.git"
set :repository_cache,    "#{application}_cache"
set :environment,         "production"
set :use_sudo, true
set :runner,              "ubuntu"
set :user,                "ubuntu"
set :scm_user,            "ubuntu"
set :deploy_to,           "/var/www/#{application}"
set :keep_releases,       2
set :deploy_via,          :remote_cache
set :scm,                 :git

role :web, "ca.thattommyhall.com"

after 'deploy:setup', :custom_setup

task :custom_setup do
  sudo "chown -R ubuntu:ubuntu #{deploy_to}"
  sudo "apt-get update"
  sudo "apt-get install -y --force-yes git build-essential openjdk-7-jre-headless"
end

task :update_upstart do
  put File.read(File.join(File.dirname(__FILE__), 'upstart')), '/tmp/upstart', :mode => '644'
  sudo "mv -f /tmp/upstart /etc/init/life.conf"
end

namespace :nginx do
  task :reload do
    sudo "service nginx reload"
  end
end

before "deploy:restart", :update_upstart, "deploy:compile_js"
after "deploy:restart", "deploy:cleanup", "nginx:reload"

namespace :deploy do
  task :compile_js do
    run "cd #{release_path} && lein cljsbuild once"
  end
  task :restart do
    sudo "stop ca;true"
    sudo "start ca"
  end
end
