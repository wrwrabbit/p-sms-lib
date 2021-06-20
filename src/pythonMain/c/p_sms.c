/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

#include <Python.h>

#include "libp_sms_api.h"

#define __ libp_sms_symbols()->
#define T_(name) libp_sms_kref_by_cyberpartisan_psms_ ## name

// Note, that as we cache this in the global, and Kotlin/Native object references
// are currently thread local, we make this global a TLS variable.
#ifdef _MSC_VER
#define TLSVAR __declspec(thread)
#else
#define TLSVAR __thread
#endif

T_(PSmsEncryptor) createEncryptor() {
    return __ kotlin.root.by.cyberpartisan.psms.PSmsEncryptor.PSmsEncryptor();
}

static PyObject* encode(PyObject* self, PyObject* args) {
    PyObject *result = Py_None;
    const char* str = NULL;
    const char* stringKey = NULL;
    int encryptionSchemeId = 0;
    if (PyArg_ParseTuple(args, "ssi", &str, &stringKey, &encryptionSchemeId)) {
        const char* encrypted = __ kotlin.root.by.cyberpartisan.psms.PSmsEncryptor.encode_(createEncryptor(), str, stringKey, encryptionSchemeId);
        result = Py_BuildValue("s", encrypted);
    }
    return result;
}

static PyObject* tryDecode(PyObject* self, PyObject* args) {
    PyObject *result = Py_None;
    const char* str = NULL;
    const char* stringKey = NULL;
    if (PyArg_ParseTuple(args, "ss", &str, &stringKey)) {
        const char* decoded = __ kotlin.root.by.cyberpartisan.psms.PSmsEncryptor.tryDecode_(createEncryptor(), str, stringKey);
        result = Py_BuildValue("s", decoded);
    }
    return result;
}

static PyObject* isEncrypted(PyObject* self, PyObject* args) {
    PyObject *result = Py_None;
    const char* str = NULL;
    const char* stringKey = NULL;
    if (PyArg_ParseTuple(args, "ss", &str, &stringKey)) {
        int encrypted = __ kotlin.root.by.cyberpartisan.psms.PSmsEncryptor.isEncrypted_(createEncryptor(), str, stringKey);
        result = encrypted ? Py_True: Py_False;
    }
    return result;
}

static PyMethodDef p_sms_funcs[] = {
   { "encode", (PyCFunction)encode, METH_VARARGS, "encode" },
   { "tryDecode", (PyCFunction)tryDecode, METH_VARARGS, "tryDecode" },
   { "isEncrypted", (PyCFunction)isEncrypted, METH_VARARGS, "isEncrypted" },
   { NULL }
};

#if PY_MAJOR_VERSION >= 3

struct module_state {
};

static int p_sms_traverse(PyObject *m, visitproc visit, void *arg) {
    return 0;
}

static int p_sms_clear(PyObject *m) {
    return 0;
}

static struct PyModuleDef moduledef = {
        PyModuleDef_HEAD_INIT,
        "p_sms",
        NULL,
        sizeof(struct module_state),
        p_sms_funcs,
        NULL,
        p_sms_traverse,
        p_sms_clear,
        NULL
};

PyMODINIT_FUNC PyInit_p_sms(void) {
   PyObject *module = PyModule_Create(&moduledef);
   return module;
}
#else
void initp_sms(void) {
   Py_InitModule3("p_sms", p_sms_funcs, "P-SMS encryption library");
}
#endif