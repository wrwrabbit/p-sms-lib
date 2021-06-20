from distutils.core import setup, Extension

setup(name='p_sms',
      version='1.0',
      maintainer = 'cyberpartisan',
      author = 'vivabelarus',
      description = 'P-SMS encryption library',

      # data_files=[("/Library/Python/3.8/site-packages/", ['libserver.dylib'])],

      ext_modules=[
         Extension('p_sms',
            include_dirs = ['./build/bin/python/releaseShared/'],
            libraries = ['p_sms'],
            library_dirs = ['./build/bin/python/releaseShared/'],
            depends = ['libp_sms_api.h'],
            sources = ['src/pythonMain/c/p_sms.c']
        )
      ]
)
