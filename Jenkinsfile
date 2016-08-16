node {
    def win = System.properties['os.name'].startsWith('Windows')

    stage 'Stage Checkout'
    checkout scm
    if (win) {
        bat 'git submodule update --init'
    } else {
        sh 'git submodule update --init'
    }

    stage 'Stage Build'
    def ver = version()
    echo "Building version ${ver} on branch ${env.BRANCH_NAME}"
    if (win) {
        bat "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
    } else {
        sh "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
    }

    stage 'Stage Archive'
    step([$class: 'ArtifactArchiver', artifacts: 'build/libs/*.jar', excludes: 'build/libs/*-base.jar',
          fingerprint: true])
}

def version() {
  def matcher = readFile('build.gradle') =~ 'version = \'(.+)\''
  matcher ? matcher[0][1] : null
}
